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
| created: 2025-02-27 ||||||||||"""
from typing import Dict, List, Set

from map.full_sample_graph_class import FullSampleGraph, ValueNode
from map.hidden_node_class import AbstractHiddenNode, HiddenNode, ConcreteHiddenNode
from map.io_node_class import IoNode
from map.knowledge_graph_class import KnowledgeGraph
from yed.parsing.parsed_edge_class import ParsedEdge, ParsedEdgeType
from yed.parsing.parsed_node_class import ParsedNode


class RawSample:
    def __init__(self,
                 sample_node: ParsedNode,
                 parsed_nodes: Dict[str, ParsedNode],
                 graph_components: List[Set[str]],
                 parsed_edges: List[ParsedEdge]):

        component: Set[str] = next(iter([c for c in graph_components if sample_node.id in c]), None)

        self.sample_nodes: Dict[str, ParsedNode] = {
            id: node for id, node in parsed_nodes.items()
            if id in component and node.node_type in ['A', 'C']}

        found_keys: Set[str] = set(self.sample_nodes.keys()) | {sample_node.id}
        assert found_keys == component, f"Not all sample nodes found, found: {found_keys}, required: {component}"

        self.sample_edges: List[ParsedEdge] = [
            edge for edge in parsed_edges
            if (edge.source_id in self.sample_nodes or edge.target_id in self.sample_nodes)
               and edge.edge_type in [ParsedEdgeType.LinkEdge, ParsedEdgeType.ThenEdge]]

        edge_end_not_in_component: Set[ParsedEdge] =  {
            edge for edge in self.sample_edges
            if edge.source_id not in self.sample_nodes or edge.target_id not in self.sample_nodes}

        assert not edge_end_not_in_component, f"Edges with end not in component found: {edge_end_not_in_component}"

        self.name: str = sample_node.name
        self.description: str = sample_node.description
        self.probability_count: int = sample_node.probability_count
        self.utility: float = sample_node.utility

    def build_full_sample(self, knowledge_graph: 'KnowledgeGraph') -> 'FullSampleGraph':
        sample: FullSampleGraph = FullSampleGraph.create_empty(
            self.probability_count,
            self.utility,
            None,
            self.name,
            self.description)

        value_nodes: Dict[str, ValueNode] = {}

        for node_id, node in self.sample_nodes.items():
            if node.node_type in ["A", "C"]:
                variable_nodes: List[HiddenNode] = knowledge_graph.get_variable_nodes_for_name(node.name)

                assert len(variable_nodes) <= 1, \
                    f"Expected 0 or 1 abstract variable node for name '{node.name}', found {len(variable_nodes)}"
            else:
                variable_nodes = []

            match node.node_type:
                case "A":
                    for variable_node in variable_nodes:
                        assert isinstance(variable_node, AbstractHiddenNode), \
                            f"Expected abstract variable node for name '{node.name}', found {variable_node}"

                    value_nodes[node.id] = sample.add_abstract_value_node(
                        variable_nodes[0] if variable_nodes else None,
                        node.name)

                case "C":
                    match variable_nodes:
                        case []:
                            io_nodes: List[IoNode] = knowledge_graph.get_io_nodes_for_name(node.variable_name)

                            assert len(io_nodes) == 1, \
                                f"Expected 1 IO node for name '{node.variable_name}', found {len(io_nodes)}"

                            value_nodes[node.id] = sample.add_concrete_value_node(
                                io_nodes[0],
                                io_nodes[0].variable.index_for_value(node.value),
                                None,
                                node.name)

                        case [variable_node]:
                            assert isinstance(variable_node, ConcreteHiddenNode), \
                                f"Expected concrete variable node for name '{node.name}', found {variable_node}"

                            concrete_node: ConcreteHiddenNode = variable_node
                            defined_value_index: int = variable_node.io_node.variable.index_for_value(node.value)

                            assert concrete_node.io_node.name == node.variable_name, \
                                (f"Variable name mismatch for node '{node.name}', "
                                 f"expected {concrete_node.io_node.name}, found {node.variable_name}")

                            assert defined_value_index == concrete_node.value_index, \
                                (f"Value index mismatch for node '{node.name}', expected {concrete_node.value_index},"
                                 f" found {defined_value_index} (for vakue '{node.value}')")

                            value_nodes[node.id] = sample.add_concrete_value_node(
                                concrete_node.io_node,
                                defined_value_index,
                                concrete_node,
                                node.name)

                        case nodes:
                            raise ValueError(
                                f"Expected 0 or 1 concrete variable node for name '{node.name}', "
                                f"found {len(nodes)}")

                case _:
                    raise ValueError(f"Unknown node type: {node.node_type}")

        for edge in self.sample_edges:
            source: ValueNode = value_nodes[edge.source_id]
            target: ValueNode = value_nodes[edge.target_id]

            match edge.edge_type:
                case ParsedEdgeType.LinkEdge:
                    sample.add_link_relation_edge(source, target)

                case ParsedEdgeType.ThenEdge:
                    sample.add_then_relation_edge(source, target)

                case _:
                    raise ValueError(f"Unknown edge type: {edge.edge_type}")

        return sample

    def __str__(self):
        return f"Sample '{self.name}' with {len(self.sample_nodes)} nodes and {len(self.sample_edges)} edges"

    def __repr__(self):
        return self.__str__()