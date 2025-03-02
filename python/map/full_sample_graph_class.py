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
| created:  2024-12-18 |||||||||"""

from typing import List

from map.hidden_node_class import AbstractHiddenNode, ConcreteHiddenNode
from map.io_node_class import IoNode


class ValueNode:
    def __init__(self, name: str | None):
        self.name: str | None = name


class AbstractValueNode(ValueNode):
    def __init__(self, hidden_node: AbstractHiddenNode | None, time_index: int | None, name: str | None):
        super().__init__(name)
        self.hidden_node: AbstractHiddenNode | None = hidden_node
        self.time_index: int | None = time_index

    def __str__(self):
        return (f"AbstractValueNode(name: {self.name}, "
                f"variable_node_name {self.hidden_node.name if self.hidden_node else "None"}, "
                f"time index: {self.time_index})")

    def __repr__(self):
        return self.__str__()


class ConcreteValueNode(ValueNode):
    def __init__(
            self,
            io_node: IoNode,
            value_index: int,
            hidden_node: ConcreteHiddenNode | None,
            time_index: int | None,
            name: str | None):

        super().__init__(name)

        assert io_node is not None, " IO node should be defined"
        assert value_index is not None, "Value index should be defined"

        self.io_node: IoNode = io_node
        self.value_index: int = value_index
        self.hidden_node: ConcreteHiddenNode | None = hidden_node
        self.time_index: int | None = time_index

    def __str__(self):
        return (f"ConcreteValueNode(name: {self.name}, "
                f"IO node name: {self.io_node.name}, "
                f"value index: {self.value_index}, "
                f"value: {self.io_node.variable.value_for_index(self.value_index)}, "
                f"variable_node_name {self.hidden_node.name if self.hidden_node else "None"}, "
                f"time index: {self.time_index})")

    def __repr__(self):
        return self.__str__()


class RelationEdge:
    def __init__(self, source: ValueNode, target: ValueNode):
        assert source is not None, "Source should be defined"
        assert target is not None, "Target should be defined"

        self.source_value_node: ValueNode = source
        self.target_value_node: ValueNode = target


class LinkRelationEdge(RelationEdge):
    def __init__(self, source: ValueNode, target: ValueNode):
        super().__init__(source, target)

    def __str__(self):
        return f"LinkRelationEdge({self.source_value_node.name} --LINK-> {self.target_value_node.name})"

    def __repr__(self):
        return self.__str__()


class ThenRelationEdge(RelationEdge):
    def __init__(self, source: ValueNode, target: ValueNode):
        super().__init__(source, target)

    def __str__(self):
        return f"ThenRelationEdge({self.source_value_node.name} --THEN-> {self.target_value_node.name})"

    def __repr__(self):
        return self.__str__()


class SampleData:
    def __init__(self,
                 sample_id: int,
                 probability_count: int,
                 utility:  float, name: str | None = None,
                 description: str | None = None):
        assert sample_id is not None, "Sample ID should be defined"
        assert probability_count is not None, "Probability count should be defined"
        assert utility is not None, "Utility should be defined"

        self.sample_id: int = sample_id
        self.probability_count: int = probability_count
        self.utility: float = utility
        self.name: str | None = name
        self.description: str | None = description

    def __str__(self):
        return (f"SampleData(sample_id: {self.sample_id}, "
                f"probability_count: {self.probability_count}, "
                f"utility: {self.utility}, "
                f"name: {self.name}, "
                f"description: {self.description})")

    def __repr__(self):
        return self.__str__()


class FullSampleGraph:
    @staticmethod
    def create_empty(
            sample_id: int,
            probability_count: int,
            utility: float,
            name: str | None = None,
            description: str | None = None) -> 'FullSampleGraph':

        data: SampleData = SampleData(sample_id, probability_count, utility, name, description)
        return FullSampleGraph(data, [], [])

    def __init__(self, data: SampleData, value_nodes: List[ValueNode], relation_edges: List[RelationEdge]):
        self.data: SampleData = data
        self.value_nodes: list[ValueNode] = value_nodes
        self.relation_edges: list[RelationEdge] = relation_edges

    def add_abstract_value_node(self,
                                hidden_node: AbstractHiddenNode | None,
                                name: str | None) -> AbstractValueNode:

        node = AbstractValueNode(hidden_node, None, name)
        self.value_nodes.append(node)
        return node

    def add_concrete_value_node(self,
                                io_node: IoNode,
                                value_index: int,
                                hidden_node: ConcreteHiddenNode | None,
                                name: str | None) -> ConcreteValueNode:

        node = ConcreteValueNode(io_node, value_index, hidden_node, None, name)
        self.value_nodes.append(node)
        return node

    def add_link_relation_edge(self, source: ValueNode, target: ValueNode) -> LinkRelationEdge:
        edge = LinkRelationEdge(source, target)
        self.relation_edges.append(edge)
        return edge

    def add_then_relation_edge(self, source: ValueNode, target: ValueNode) -> ThenRelationEdge:
        edge = ThenRelationEdge(source, target)
        self.relation_edges.append(edge)
        return edge

    def __str__(self):
        return (f"FullSampleGraph(\n"
                f"data: {self.data}\n"
                f"value_nodes:\n    {"\n    ".join([str(n) for n in self.value_nodes])}\n"
                f"relation_edges:\n    {"\n    ".join([str(e) for e in self.relation_edges])}\n)")
