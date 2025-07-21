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

from typing import Dict, Any


class MapInfo:

    @staticmethod
    def from_json(json_data: Dict[str, Any]):
        assert json_data, "JSON data should not be empty"
        assert 'mapName' in json_data, "Map name should be defined in JSON data"
        assert 'numInputNodes' in json_data, "Number of input nodes should be defined in JSON data"
        assert 'numOutputNodes' in json_data, "Number of output nodes should be defined in JSON data"
        assert 'numHiddenNodes' in json_data, "Number of hidden nodes should be defined in JSON data"

        return MapInfo(
            map_name=json_data['mapName'],
            num_input_nodes=json_data['numInputNodes'],
            num_output_nodes=json_data['numOutputNodes'],
            num_hidden_nodes=json_data['numHiddenNodes']
        )

    def __init__(
            self,
            map_name: str,
            num_input_nodes: int,
            num_output_nodes: int,
            num_hidden_nodes: int):
        assert map_name, "Map name should be defined"

        self.map_name: str = map_name
        self.num_input_nodes: int = num_input_nodes
        self.num_output_nodes: int = num_output_nodes
        self.num_hidden_nodes: int = num_hidden_nodes

    def __str__(self):
        return (
            f"MapInfo("
            f"map_name={self.map_name}, "
            f"num_input_nodes={self.num_input_nodes}, "
            f"num_output_nodes={self.num_output_nodes}, "
            f"num_hidden_nodes={self.num_hidden_nodes})"
        )

    def __repr__(self):
        return self.__str__()
