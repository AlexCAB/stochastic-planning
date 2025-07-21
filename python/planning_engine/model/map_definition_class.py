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

from typing import List, Optional, Dict, Any

from planning_engine.model.io_node_class import IoNode


class MapDefinition:
    def __init__(
            self,
            db_name: str,
            name: str,
            description: Optional[str],
            input_nodes: List[IoNode],
            output_nodes: List[IoNode]):
        assert db_name, "DB name should be defined"
        assert name, "Name id should be defined"

        self.db_name: str = db_name
        self.name: str = name
        self.description: Optional[str] = description
        self.input_nodes: List[IoNode] = input_nodes
        self.output_nodes: List[IoNode] = output_nodes

    def to_json(self) -> Dict[str, Any]:
        return {
            "dbName": self.db_name,
            "name": self.name,
            "description": self.description,
            "inputNodes": [node.to_json() for node in self.input_nodes],
            "outputNodes": [node.to_json() for node in self.output_nodes]
        }

    def __str__(self):
        return f"MapDefinition(db_name={self.db_name}, name={self.name})"

    def __repr__(self):
        return self.__str__()
