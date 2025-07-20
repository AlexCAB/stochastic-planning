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
| created: 2025-07-19 ||||||||||"""

from typing import List, Optional


class MapDefinition:
    def __init__(self,
                 db_name: str,
                 name: str,
                 description: Optional[str],
                 input_nodes: List[IoNode],
                 output_nodes: List[IoNode]):

        assert name is not None, "Name should be defined"
        assert neo4j_id is not None, "Neo4j id should be defined"

        self.name: str = name
        self.neo4j_id: str = neo4j_id

    def __str__(self):
        return f"MapDefinition(name={self.name}, neo4j_id={self.neo4j_id})"

    def __repr__(self):
        return self.__str__()