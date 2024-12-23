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

from dataclasses import dataclass
from typing import List

from map.knowledge_graph import VariableNode
from map.variable import Value


@dataclass
class SampleId:
    neo4j_id: int


class ValueNode:
    def __init__(self, value: Value, variable_node: VariableNode | None):
        self.value: Value = value
        self.variable_node: VariableNode | None = variable_node


class InputValueNode(ValueNode):
    def __init__(self, value: Value, variable_node: VariableNode | None):
        super().__init__(value, variable_node)


class HiddenValueNode(ValueNode):
    def __init__(self, value: Value, variable_node: VariableNode | None):
        super().__init__(value, variable_node)


class OutputValueNode(ValueNode):
    def __init__(self, value: Value, variable_node: VariableNode | None):
        super().__init__(value, variable_node)


class RelationEdge:
    def __init__(self, source: ValueNode, target: ValueNode):
        self.source_value_node = source
        self.target_value_node = target


class LinkRelationEdge(RelationEdge):
    def __init__(self, source: ValueNode, target: ValueNode):
        super().__init__(source, target)


class ThenRelationEdge(RelationEdge):
    def __init__(self, source: ValueNode, target: ValueNode):
        super().__init__(source, target)


class SampleGraph:
    def __init__(self,
                 sample_id: SampleId,
                 value_nodes: List[ValueNode],
                 relation_edges: List[RelationEdge],
                 probability_count: int):
        self.sample_id: SampleId = sample_id
        self.value_nodes: list[ValueNode] = value_nodes
        self.relation_edges: list[RelationEdge] = relation_edges
        self.probability_count = probability_count
