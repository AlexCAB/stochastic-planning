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
from typing import List

from networkx.classes import MultiDiGraph
from networkx.readwrite.graphml import read_graphml


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class ParsedNode:
    def __init__(self, id: str, label: str):
        split_label = label.split(":")

        self.id: str = id
        self.node_type: str = split_label[0]
        self.name: str = ""
        self.description: str | None = None
        self.probability_count: int | None = None
        self.utility: float | None = None
        self.value_type: str | None = None
        self.variable_name: str | None = None
        self.value: str | None = None


        match split_label[0]:
            case "G":
               self.name = split_label[1]
               self.description = split_label[2]

            case "S":
               self.name = split_label[1]
               self.probability_count = int(split_label[2])
               self.utility = float(split_label[3])
               self.description = split_label[4]

               assert \
                   self.probability_count is not None and self.probability_count > 0,\
                   f"Probability count is None or <= 0, label = {label}"

               assert self.utility is not None, f"Utility is None, label = {label}"

            case "I" | "O":
                self.name = split_label[1]
                self.value_type = split_label[2]
                self.description = split_label[3]

                assert \
                    self.value_type in ["bool", "int", "float", "list"], \
                    f"Value type should be one off: bool, int, float or list , label = {label}"

            case "C":
                self.variable_name = split_label[1]
                self.value = split_label[2]
                self.name = split_label[3]
                self.description = split_label[4]

                assert \
                    self.variable_name != "" and self.variable_name is not None,\
                    f"Variable name is empty or None, label = {label}"

                assert self.value != "" and self.value is not None, f"Value is empty or None, label = {label}"

            case "A":
                self.name = split_label[1]
                self.description = split_label[2]

            case _:
               raise ValueError(f"Unknown node type: {split_label[0]}, expected one of: G, S, I, O, C, A")

        assert self.node_type != "" and self.node_type is not None, f"Node type is empty or None, label = {label}"
        assert self.name != "" and self.name is not None, f"Node name is empty or None, label = {label}"

    def __str__(self):
        return f"Node: id = {self.id}, node_type = {self.node_type}, name = {self.name}, "\
            f"escription = {self.description}, probability_count = {self.probability_count}, "\
            f"utility = {self.utility}, value_type = {self.value_type}, variable_name = {self.variable_name}, "\
            "value = {self.value}"


def read_args():
    parser = argparse.ArgumentParser(description="yEd map graph loader")
    parser.add_argument(
        '--path',
        type=str,
        required=False,
        help='Path to yEd graph file (*.graphml)')
    args = parser.parse_args()
    return args

def load_yed_graph(yed_path: str):
    logger.info(f"[load_yed_graph] Loading yEd graph from {yed_path}, work dir = {os.getcwd()}")

    yed_graph: MultiDiGraph  = read_graphml(yed_path)

    parsed_nodes = {id: ParsedNode(id, props['label']) for (id, props) in yed_graph.nodes(data=True)}

    for node in parsed_nodes.values():
       print(f"Node: {node}")









def main():
    args = read_args()
    if args.path is not None:
        load_yed_graph(args.path)
    else:
        load_yed_graph("yed/example_maps/pacman-5x3-two-ways.graphml") # default path

if __name__ == "__main__":
    main()
