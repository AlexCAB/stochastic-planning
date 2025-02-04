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

from map.hidden import AbstractHiddenNode, ConcreteHiddenNode
from map.in_out import IoNode



class ValueNode:
    pass

class AbstractValueNode(ValueNode):
    def __init__(self, hidden_node: AbstractHiddenNode | None, time_index: int | None):
        self.hidden_node: AbstractHiddenNode = hidden_node
        self.time_index: int | None = time_index


class ConcreteValueNode(ValueNode):
    def __init__(
            self,
            hidden_node: ConcreteHiddenNode | None,
            time_index: int | None,
            io_node: IoNode | None,
            value_index: int):

        assert hidden_node  is not None or io_node is not None, "Hidden node or IO node should be defined"
        assert value_index is not None, "Value index should be defined"

        self.hidden_node: ConcreteHiddenNode | None = hidden_node
        self.time_index: int | None = time_index
        self.io_node: IoNode | None = io_node
        self.value_index: int = value_index


class RelationEdge:
    def __init__(self, source: ValueNode, target: ValueNode):
        assert source is not None, "Source should be defined"
        assert target is not None, "Target should be defined"

        self.source_value_node: ValueNode = source
        self.target_value_node: ValueNode = target


class LinkRelationEdge(RelationEdge):
    def __init__(self, source: ValueNode, target: ValueNode):
        super().__init__(source, target)


class ThenRelationEdge(RelationEdge):
    def __init__(self, source: ValueNode, target: ValueNode):
        super().__init__(source, target)


class SampleData:
    def __init__(self, sample_id: int, probability_count: int, utility:  float, name: str | None = None):
        assert sample_id is not None, "Sample ID should be defined"
        assert probability_count is not None, "Probability count should be defined"
        assert utility is not None, "Utility should be defined"

        self.sample_id: int = sample_id
        self.probability_count: int = probability_count
        self.utility: float = utility
        self.name: str | None = name


class FullSampleGraph:
    def __init__(self, data: SampleData, value_nodes: List[ValueNode], relation_edges: List[RelationEdge]):
        self.data: SampleData = data
        self.value_nodes: list[ValueNode] = value_nodes
        self.relation_edges: list[RelationEdge] = relation_edges
