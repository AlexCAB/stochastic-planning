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
| created:  2024-12-19 |||||||||"""

from typing import Dict, List, Set

from map.sample_graph import SampleId
from map.variable import *


class VariableNode:
    def __init__(self, name: str, variable: Variable, neo4j_id: str | None):
        self.name: str = name
        self.variable: Variable = variable
        self.neo4j_id: str | None = neo4j_id

        self.outgoing_link_edges: Dict[VariableNode, 'LinkJointEdge'] = {}
        self.outgoing_then_edges: Dict[VariableNode, 'ThenJointEdge'] = {}
        self.ingoing_edges: Set[VariableNode] = set()


class InputVariableNode(VariableNode):
    def __init__(self, name: str, variable: Variable, neo4j_id: str | None):
        super().__init__(name, variable, neo4j_id)


class HiddenVariableNode(VariableNode):
    def __init__(self, name: str | None, neo4j_id: str | None):
        super().__init__(name is None if name else f"Hidden variable, id = {neo4j_id}", TimeVariable(), neo4j_id)


class OutputVariableNode(VariableNode):
    def __init__(self, name: str, variable: Variable, neo4j_id: str | None):
        super().__init__(name, variable, neo4j_id)


class SampleEdge:
    def __init__(self, source_index: int, target_index: int, probability_count: int):
        self.source_value_index: int = source_index
        self.target_value_index: int = target_index
        self.probability_count = probability_count


class JointEdge:
    def __init__(self, samples: Dict[SampleId, SampleEdge]):
        self.samples_dict: Dict[SampleId, SampleEdge] = samples


class LinkJointEdge(JointEdge):
    def __init__(self, samples: Dict[SampleId, SampleEdge]):
        super().__init__(samples)


class ThenJointEdge(JointEdge):
    def __init__(self, samples: Dict[SampleId, SampleEdge]):
        super().__init__(samples)


class Metadata:
    def __init__(self, num_of_samples: int):
        self.num_of_samples: int = num_of_samples


class KnowledgeGraph:
    def __init__(self, metadata: Metadata, variable_nodes: List[VariableNode]):
        self.metadata: Metadata = metadata
        self.variable_nodes: Dict[str, VariableNode] = {node.neo4j_id: node for node in variable_nodes}
