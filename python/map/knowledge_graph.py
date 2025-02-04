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
from map.in_out import *


class Metadata:
    def __init__(self, num_of_samples: int, sample_data: List[SampleData]):
        assert num_of_samples is not None, "Number of samples should be defined"
        assert sample_data is not None, "Sample data should be defined"

        self.num_of_samples: int = num_of_samples
        self.sample_data: Dict[int, SampleData] = {data.sample_id: data for data in sample_data}


class KnowledgeGraph:
    def __init__(
            self, metadata: Metadata,
            input: List[InputNode],
            output: List[OutputNode],
            hidden: List[HiddenNode],
            relations: List[RelationEdge]):

        assert metadata is not None, "Metadata should be defined"
        assert input is not None, "Input should be defined"
        assert output is not None, "Output should be defined"
        assert hidden is not None, "Hidden should be defined"
        assert relations is not None, "Relations should be defined"

        self.metadata: Metadata = metadata
        self.input: List[InputNode] = input
        self.output: List[OutputNode] = output
        self.hidden: List[HiddenNode] = hidden
        self.relations: List[RelationEdge] = relations
