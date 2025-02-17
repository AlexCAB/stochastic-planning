#!/usr/bin/python
# -*- coding: utf-8 -*-
r"""|||||||||||||||||||||||||||||||
|| 0 * * * * * * * * * ▲ * * * * ||
|| * ||||||||||| * ||||||||||| * ||
|| * ||  * * * * * ||       || 0 ||
|| * ||||||||||| * ||||||||||| * ||
|| * * ▲ * * 0|| * ||   (< * * * ||
|| * ||||||||||| * ||  ||||||||||||
|| * * * * * * * * *   ||||||||||||
| author: CAB |||||||||||||||||||||
| website: github.com/alexcab |||||
| created:  2024-12-26 |||||||||"""

import argparse
import os
import logging
from typing import List, Dict, Set, Any, Tuple

from networkx.algorithms.components import weakly_connected_components
from networkx.classes import MultiDiGraph
from networkx.readwrite.graphml import read_graphml

from map.io_node import InputNode, OutputNode
from map.io_variable import IoVariable
from map.knowledge_graph import KnowledgeGraph, Metadata

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class ParsedNode:
    def __init__(self, id: str, label: str):
        split_label = label.split(":")

        self.id: str = id
        self.node_type: str = split_label[0].strip()

        self.name: str = ""
        self.description: str | None = None
        self.probability_count: int | None = None
        self.utility: float | None = None
        self.value_type: type | None = None
        self.value_range: List[bool] | Tuple[int, int] | Tuple[float, float] | List[Any] | None = None
        self.variable_name: str | None = None
        self.value: str | None = None

        match split_label[0]:
            case "G":
               self.name = split_label[1].strip()
               self.description = ":".join(split_label[2:])

            case "S":
               self.name = split_label[1].strip()
               self.probability_count = int(split_label[2].strip())
               self.utility = float(split_label[3].strip())
               self.description = ":".join(split_label[4:])

               assert \
                   self.probability_count is not None and self.probability_count > 0,\
                   f"Probability count is None or <= 0, label = {label}"

               assert self.utility is not None, f"Utility is None, label = {label}"

            case "I" | "O":
                self.name = split_label[1].strip()
                split_value_range = [v.strip() for v in split_label[3].split(";")]


                match split_label[2]:
                    case "bool":
                        self.value_type = bool
                        self.value_range = [bool(v) for v in split_value_range]

                        assert \
                            0 < len(self.value_range) <= 2, \
                            f"Invalid value range: {split_label[3]}, for type `bool` expected 1 or 2 values, "\
                            "label = {label}"

                    case "int":
                        self.value_type = int
                        value_range = [int(v) for v in split_value_range]

                        assert \
                            len(value_range) == 2, \
                            f"Invalid value range: {split_label[3]}, for type `int` expected 2 values (min, max), "\
                            "label = {label}"

                        self.value_range = (value_range[0], value_range[1])

                    case "float":
                        self.value_type = float
                        value_range: List[float] = [float(v) for v in split_value_range]

                        assert \
                            len(value_range) == 2, \
                            f"Invalid value range: {split_label[3]}, for type `float` expected 2 values (min, max), "\
                            "label = {label}"

                        self.value_range = (value_range[0], value_range[1])

                    case "list":
                        self.value_type = list
                        self.value_range = split_value_range

                        assert self.value_range, f"List of values is empty, label = {label}"

                    case _:
                        raise ValueError(
                            f"Unknown value type: {split_label[2]}, expected one of: bool, int, float or list, "
                            f"label = {label}")

                self.description = ":".join(split_label[4:])

            case "C":
                self.variable_name = split_label[1].strip()
                self.value = split_label[2].strip()
                self.name = split_label[3].strip()
                self.description = ":".join(split_label[4:])

                assert \
                    self.variable_name != "" and self.variable_name is not None,\
                    f"Variable name is empty or None, label = {label}"

                assert self.value != "" and self.value is not None, f"Value is empty or None, label = {label}"

            case "A":
                self.name = split_label[1].strip()
                self.description = ":".join(split_label[2:])

            case _:
               raise ValueError(f"Unknown node type: {split_label[0]}, expected one of: G, S, I, O, C, A")

        assert self.node_type != "" and self.node_type is not None, f"Node type is empty or None, label = {label}"
        assert self.name != "" and self.name is not None, f"Node name is empty or None, label = {label}"

        self.description = self.description if self.description != "" else None

    def __str__(self):
        return f"Node: id = {self.id}, node_type = {self.node_type}, name = {self.name}, "\
            f"description = {self.description}, probability_count = {self.probability_count}, "\
            f"utility = {self.utility}, value_type = {self.value_type}, value_range = {self.value_range}, "\
            f"variable_name = {self.variable_name}, value = {self.value}"


def read_args():
    parser = argparse.ArgumentParser(description="yEd map graph loader")
    parser.add_argument(
        '--path',
        type=str,
        required=False,
        help='Path to yEd graph file (*.graphml)')
    args = parser.parse_args()
    return args

def build_knowledge_graph(parsed_nodes: Dict[str, ParsedNode], graph_components: List[Set[str]]) -> KnowledgeGraph:
    graph_node: ParsedNode = next(filter(lambda n: n.node_type == "G", parsed_nodes.values()), None)
    assert graph_node is not None, "Graph node should be defined"
    component: Set[str] = next((c for c in graph_components if graph_node.id in c), None)
    assert component is not None, "Graph node should be in one of the components"
    metadata: Metadata = Metadata(graph_node.name, 0, [], graph_node.description)

    in_parsed_nodes: List[ParsedNode] = [parsed_nodes[id] for id in component if parsed_nodes[id].node_type == "I"]
    assert len(in_parsed_nodes) > 0, "At least one input node should be defined"
    out_parsed_nodes: List[ParsedNode] = [parsed_nodes[id] for id in component if parsed_nodes[id].node_type == "O"]
    assert len(out_parsed_nodes) > 0, "At least one output node should be defined"

    input_nodes: List[InputNode] = [
        InputNode(node.name, IoVariable.create_variable(node.value_type, node.value_range))
        for node in in_parsed_nodes]

    output_nodes: List[OutputNode] = [
        OutputNode(node.name, IoVariable.create_variable(node.value_type, node.value_range))
        for node in out_parsed_nodes]

    return KnowledgeGraph(metadata, input_nodes, output_nodes)








def load_yed_graph(yed_path: str):
    logger.info(f"[load_yed_graph] Loading yEd graph from {yed_path}, work dir = {os.getcwd()}")

    yed_graph: MultiDiGraph  = read_graphml(yed_path)

    parsed_nodes: Dict[str, ParsedNode] = {id: ParsedNode(id, props['label']) for (id, props) in yed_graph.nodes(data=True)}

    graph_components: List[Set[str]] = list(weakly_connected_components(yed_graph))

    for node in parsed_nodes.values():
        print(f"Node: {node}")

    for com in weakly_connected_components(yed_graph):
        print(f"Connected component: {com}")

    knowledge_graph: KnowledgeGraph = build_knowledge_graph(parsed_nodes, graph_components)

    print(knowledge_graph)










def main():
    args = read_args()
    if args.path is not None:
        load_yed_graph(args.path)
    else:
        load_yed_graph("yed/example_maps/pacman-5x3-two-ways.graphml") # default path

if __name__ == "__main__":
    main()
