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

from map.knowledge_graph import VariableNode, VariableNodeId
from map.variable import Value


@dataclass
class SampleId:
    neo4j_id: str


class ValueNode:
    def __init__(self, value: Value, variable_node_id: VariableNodeId):
        self.value: Value = value
        self.variable_node_id: VariableNodeId = variable_node_id


class InputValueNode(ValueNode):
    def __init__(self, value: Value, variable_node_id: VariableNodeId):
        super().__init__(value, variable_node_id)


class HiddenValueNode(ValueNode):
    def __init__(self, value: Value, variable_node_id: VariableNodeId):
        super().__init__(value, variable_node_id)


class OutputValueNode(ValueNode):
    def __init__(self, value: Value, variable_node_id: VariableNodeId):
        super().__init__(value, variable_node_id)


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


class SampleData:
    def __init__(self, sample_id: SampleId, probability_count: int, utility:  float, name: str | None):
        self.sample_id: SampleId = sample_id
        self.probability_count: int = probability_count
        self.utility: float = utility
        self.name: str | None = name


class SampleGraph:
    def __init__(self,data: SampleData, value_nodes: List[ValueNode], relation_edges: List[RelationEdge]):
        self.data: SampleData = data
        self.value_nodes: list[ValueNode] = value_nodes
        self.relation_edges: list[RelationEdge] = relation_edges
