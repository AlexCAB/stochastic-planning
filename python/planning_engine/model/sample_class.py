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
| created: 2025-07-27 ||||||||||"""

from typing import Optional, List

from planning_engine.model.hidden_edge_class import HiddenEdge
from planning_engine.model.hidden_node_class import HiddenNode


class Sample:
    def __init__(
            self,
            probability_count: int,
            utility: float,
            name: Optional[str],
            description: Optional[str],
            hidden_nodes: List[HiddenNode],
            edges: List[HiddenEdge]):

        assert probability_count is not None, "Probability count should be defined"
        assert probability_count >= 0, "Probability count should be non-negative"
        assert utility is not None, "Utility should be defined"

        self.probability_count: int = probability_count
        self.utility: float = utility
        self.name: Optional[str] = name
        self.description: Optional[str] = description
        self.hidden_nodes: List[HiddenNode] = hidden_nodes
        self.edges: List[HiddenEdge] = edges

    def to_json(self)  -> dict:
        return {
            "probabilityCount": self.probability_count,
            "utility": self.utility,
            "name": self.name,
            "description": self.description,
            "hiddenNodes": [node.to_json() for node in self.hidden_nodes],
            "edges": [edge.to_json() for edge in self.edges]
        }

    def __str__(self):
        return (f"Sample(\n"
                f"    probability_count = {self.probability_count},\n"
                f"    utility = {self.utility},\n"
                f"    name = {self.name},\n"
                f"    description = {self.description},\n"
                f"    hidden_nodes = [\n        {',\n        '.join(str(n) for n in self.hidden_nodes)}\n    ],\n"
                f"    edges = [\n        {',\n        '.join(str(n) for n in self.edges)}\n    ]\n"
                f")")

    def __repr__(self):
        return self.__str__()
