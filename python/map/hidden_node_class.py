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
| created: 2025-02-04 ||||||||||"""

from typing import Dict, Tuple

from map.io_node_class import IoNode

class HiddenNode:
    pass

class AbstractHiddenNode(HiddenNode):
    def __init__(self, name: str, neo4j_id: str):
        assert name is not None, "Name should be defined"
        assert neo4j_id is not None, "Neo4j id should be defined"

        self.name: str = name
        self.neo4j_id: str = neo4j_id


class ConcreteHiddenNode(AbstractHiddenNode):
    def __init__(self, name: str, neo4j_id: str, io_node: IoNode):
        super().__init__(name, neo4j_id)
        self.io_node: IoNode = io_node


class HiddenEdge:
    def __init__(self, source: HiddenNode, target: HiddenNode, samples: Dict[int, Tuple[int, int]]):
        self.source: HiddenNode = source
        self.target: HiddenNode = target
        self.samples: Dict[int, (int, int)] = samples # sample id -> (source time index, target time index)


class LinkHiddenEdge(HiddenEdge):
    def __init__(self, source: HiddenNode, target: HiddenNode, samples: Dict[int, Tuple[int, int]]):
        super().__init__(source, target, samples)


class ThenHiddenEdge(HiddenEdge):
    def __init__(self, source: HiddenNode, target: HiddenNode, samples: Dict[int, Tuple[int, int]]):
        super().__init__(source, target, samples)
