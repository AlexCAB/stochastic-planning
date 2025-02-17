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

from map.full_sample_graph import *
from map.hidden import *
from map.io_node import *


class Metadata:
    def __init__(self, name: str, num_of_samples: int, sample_data: List[SampleData], description: str | None = None):
        assert name is not None, "Name should be defined"
        assert num_of_samples is not None, "Number of samples should be defined"
        assert sample_data is not None, "Sample data should be defined"

        self.name: str = name
        self.num_of_samples: int = num_of_samples
        self.sample_data: Dict[int, SampleData] = {data.sample_id: data for data in sample_data}
        self.description: str | None = description

    def __str__(self):
        return (f"Metadata: {self.name}\n"
                f"Description: {self.description}\n"
                f"Number of samples: {self.num_of_samples}\n")


class KnowledgeGraph:
    def __init__(
            self,
            metadata: Metadata,
            input: List[InputNode],
            output: List[OutputNode],
            hidden: List[HiddenNode] = None,
            relations: List[RelationEdge] = None):

        assert metadata is not None, "Metadata should be defined"
        assert input is not None, "Input should be defined"
        assert output is not None, "Output should be defined"

        self.metadata: Metadata = metadata
        self.input: List[InputNode] = input
        self.output: List[OutputNode] = output
        self.hidden: List[HiddenNode] = hidden if hidden is not None else []
        self.relations: List[RelationEdge] = relations if relations is not None else []

    def __str__(self):
        return (f"Knowledge graph: '{self.metadata.name}'\n"
                f"Description: {self.metadata.description}\n"
                f"Input nodes:\n  {"\n  ".join([str(n) for n in self.input])}\n"
                f"Output nodes:\n  {"\n  ".join([str(n) for n in self.output])}")
