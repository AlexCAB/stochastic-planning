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

from typing import Dict, Set

from map.sample_graph import *
from map.variable import *

@dataclass
class VariableNodeId:
    neo4j_id: str | None
    manual_id: str | None  # In case 'neo4j_id' is None this ID will be used for lookup


class VariableNode:
    def __init__(self, name: str, variable: Variable, node_id: VariableNodeId):
        self.name: str = name
        self.variable: Variable = variable
        self.variable_node_id: VariableNodeId = node_id

        self.outgoing_link_edges: Dict[VariableNode, 'LinkJointEdge'] = {}
        self.outgoing_then_edges: Dict[VariableNode, 'ThenJointEdge'] = {}
        self.ingoing_edges: Set[VariableNode] = set()


class InputVariableNode(VariableNode):
    def __init__(self, name: str, variable: Variable, node_id: VariableNodeId):
        super().__init__(name, variable, node_id)


class HiddenVariableNode(VariableNode):
    def __init__(self, name: str | None, node_id: VariableNodeId):
        super().__init__(name is None if name else f"Hidden variable, id = {node_id}", TimeVariable(), node_id)


class OutputVariableNode(VariableNode):
    def __init__(self, name: str, variable: Variable, node_id: VariableNodeId):
        super().__init__(name, variable, node_id)


class SampleEdge:
    def __init__(self, source_index: int, target_index: int):
        self.source_value_index: int = source_index
        self.target_value_index: int = target_index


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
    def __init__(self, metadata: Metadata, variable_nodes: List[VariableNode], sample_data: List[SampleData]):
        self.metadata: Metadata = metadata
        self.variable_nodes: Dict[str, VariableNode] = {node.variable_node_id.neo4j_id: node for node in variable_nodes}
        self.sample_data: Dict[str, SampleData] = {data.sample_id.neo4j_id: data for data in sample_data}