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
| created: 2025-07-21 ||||||||||"""

from typing import Dict, Any, Optional


class MapInfo:

    @staticmethod
    def from_json(json_data: Dict[str, Any]) -> 'MapInfo':
        assert json_data, "JSON data should not be empty"
        assert 'dbName' in json_data, "DB name should be defined in JSON data"
        assert 'numInputNodes' in json_data, "Number of input nodes should be defined in JSON data"
        assert 'numOutputNodes' in json_data, "Number of output nodes should be defined in JSON data"
        assert 'numHiddenNodes' in json_data, "Number of hidden nodes should be defined in JSON data"

        return MapInfo(
            db_name=json_data['dbName'],
            map_name=json_data['mapName'],
            num_input_nodes=json_data['numInputNodes'],
            num_output_nodes=json_data['numOutputNodes'],
            num_hidden_nodes=json_data['numHiddenNodes']
        )

    def __init__(
            self,
            db_name: str,
            map_name: Optional[str],
            num_input_nodes: int,
            num_output_nodes: int,
            num_hidden_nodes: int):
        assert db_name, "Map DB name should be defined"

        self.db_name: str = db_name
        self.map_name:  Optional[str] = map_name
        self.num_input_nodes: int = num_input_nodes
        self.num_output_nodes: int = num_output_nodes
        self.num_hidden_nodes: int = num_hidden_nodes

    def __str__(self):
        return (
            f"MapInfo(\n"
            f"    db_name = {self.db_name},\n"
            f"    map_name = {self.map_name},\n"
            f"    num_input_nodes = {self.num_input_nodes},\n"
            f"    num_output_nodes = {self.num_output_nodes},\n"
            f"    num_hidden_nodes = {self.num_hidden_nodes}\n"
            f")"
        )

    def __repr__(self):
        return self.__str__()
