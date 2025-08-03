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

from typing import Optional, List, Dict, Any

from planning_engine.model.hidden_edge_class import HiddenEdge
from planning_engine.model.hidden_node_class import HiddenNode


class Sample:
    def __init__(
            self,
            probability_count: int,
            utility: float,
            name: Optional[str],
            description: Optional[str],
            edges: List[HiddenEdge]):
        assert probability_count is not None, "Probability count should be defined"
        assert probability_count >= 0, "Probability count should be non-negative"
        assert utility is not None, "Utility should be defined"

        self.probability_count: int = probability_count
        self.utility: float = utility
        self.name: Optional[str] = name
        self.description: Optional[str] = description

        self.edges: List[HiddenEdge] = edges

    def to_json(self) -> Dict[str, Any]:
        return {
            "probabilityCount": self.probability_count,
            "utility": self.utility,
            "name": self.name,
            "description": self.description,
            "edges": [edge.to_json() for edge in self.edges]
        }

    def __str__(self):
        return (f"Sample(\n"
                f"      probability_count = {self.probability_count},\n"
                f"      utility = {self.utility},\n"
                f"      name = {self.name},\n"
                f"      description = {self.description},\n"
                f"      edges = [\n        {',\n        '.join(str(n) for n in self.edges)}\n      ]\n"
                f"    )")

    def __repr__(self):
        return self.__str__()


class Samples:
    def __init__(self, hidden_nodes: List[HiddenNode], samples: List[Sample]):
        hn_names = {node.name for node in hidden_nodes}

        assert hidden_nodes, "Hidden nodes list should not be empty"
        assert len(hn_names) == len(hidden_nodes), "Hidden nodes should have unique names"
        assert samples, "Samples list should not be empty"

        for node in hidden_nodes:
            assert isinstance(node, HiddenNode), f"Expected HiddenNode instance, got {type(node)}"

        for sample in samples:
            assert isinstance(sample, Sample), f"Expected Sample instance, got {type(sample)}"

            he_names = set()
            for s in samples:
                for e in s.edges:
                    he_names.add(e.source_hn_name)
                    he_names.add(e.target_hn_name)

            assert he_names.issubset(hn_names), f"All edge nodes must be in hidden nodes. Found: {he_names - hn_names}"

        self.hidden_nodes: List[HiddenNode] = hidden_nodes
        self.samples: List[Sample] = samples

    def to_json(self) -> Dict[str, Any]:
        return {
            "hiddenNodes": [node.to_json() for node in self.hidden_nodes],
            "samples": [sample.to_json() for sample in self.samples]
        }

    def __str__(self):
        return (f"Samples(\n"
                f"  hidden_nodes = [\n    {',\n    '.join(str(n) for n in self.hidden_nodes)}\n  ],\n"
                f"  samples = [\n    {',\n    '.join(str(s) for s in self.samples)}\n  ]\n)")

    def __repr__(self):
        return self.__str__()
