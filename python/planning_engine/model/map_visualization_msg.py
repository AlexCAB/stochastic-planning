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
| created: 2025-12-31 ||||||||||"""

from typing import Dict, Any, Set
import json


class MapVisualizationMsg:

    @staticmethod
    def from_raw_json(json_str: str) -> MapVisualizationMsg:
        assert json_str, "JSON data should not be empty"

        json_data = json.loads(json_str)

        assert 'inNodes' in json_data, "Input nodes should be defined in JSON data"
        assert 'outNodes' in json_data, "Output nodes should be defined in JSON data"
        assert 'ioValues' in json_data, "IO values should be defined in JSON data"
        assert 'concreteNodes' in json_data, "Concrete nodes should be defined in JSON data"
        assert 'abstractNodes' in json_data, "Abstract nodes should be defined in JSON data"
        assert 'forwardLinks' in json_data, "Forward links should be defined in JSON data"
        assert 'backwardLinks' in json_data, "Backward links should be defined in JSON data"
        assert 'forwardThen' in json_data, "Forward then links should be defined in JSON data"
        assert 'backwardThen' in json_data, "Backward then links should be defined in JSON data"

        return MapVisualizationMsg(
            in_nodes=set(json_data['inNodes']),
            out_nodes=set(json_data['outNodes']),
            io_values={str(e[0]): set(e[1]) for e in json_data['ioValues']},
            concrete_nodes=set(json_data['concreteNodes']),
            abstract_nodes=set(json_data['abstractNodes']),
            forward_links={int(e[0]): set(e[1]) for e in json_data['forwardLinks']},
            backward_links={int(e[0]): set(e[1]) for e in json_data['backwardLinks']},
            forward_then={int(e[0]): set(e[1]) for e in json_data['forwardThen']},
            backward_then={int(e[0]): set(e[1]) for e in json_data['backwardThen']}
        )

    def __init__(
            self,
            in_nodes: Set[str],
            out_nodes: Set[str],
            io_values: Dict[str, Set[int]],
            concrete_nodes: Set[int],
            abstract_nodes: Set[int],
            forward_links: Dict[int, Set[int]],
            backward_links: Dict[int, Set[int]],
            forward_then: Dict[int, Set[int]],
            backward_then: Dict[int, Set[int]]):
        assert in_nodes is not None, "Input nodes should be defined"
        assert isinstance(in_nodes, set), "Input nodes should be a set"
        assert out_nodes is not None, "Output nodes should be defined"
        assert isinstance(out_nodes, set), "Output nodes should be a set"
        assert io_values is not None, "IO values should be defined"
        assert isinstance(io_values, dict), "IO values should be a dictionary"
        assert concrete_nodes is not None, "Concrete nodes should be defined"
        assert isinstance(concrete_nodes, set), "Concrete nodes should be a set"
        assert abstract_nodes is not None, "Abstract nodes should be defined"
        assert isinstance(abstract_nodes, set), "Abstract nodes should be a set"
        assert forward_links is not None, "Forward links should be defined"
        assert isinstance(forward_links, dict), "Forward links should be a dictionary"
        assert backward_links is not None, "Backward links should be defined"
        assert isinstance(backward_links, dict), "Backward links should be a dictionary"
        assert forward_then is not None, "Forward then links should be defined"
        assert isinstance(forward_then, dict), "Forward then links should be a dictionary"
        assert backward_then is not None, "Backward then links should be defined"
        assert isinstance(backward_then, dict), "Backward then links should be a dictionary"

        self.in_nodes: Set[str] = in_nodes
        self.out_nodes: Set[str] = out_nodes
        self.io_values: Dict[str, Set[int]] = io_values
        self.concrete_nodes: Set[int] = concrete_nodes
        self.abstract_nodes: Set[int] = abstract_nodes
        self.forward_links: Dict[int, Set[int]] = forward_links
        self.backward_links: Dict[int, Set[int]] = backward_links
        self.forward_then: Dict[int, Set[int]] = forward_then
        self.backward_then: Dict[int, Set[int]] = backward_then

    def __eq__(self, other: Any) -> bool:
        return isinstance(other, MapVisualizationMsg) \
            and self.in_nodes == other.in_nodes \
            and self.out_nodes == other.out_nodes \
            and self.io_values == other.io_values \
            and self.concrete_nodes == other.concrete_nodes \
            and self.abstract_nodes == other.abstract_nodes \
            and self.forward_links == other.forward_links \
            and self.backward_links == other.backward_links \
            and self.forward_then == other.forward_then \
            and self.backward_then == other.backward_then

    def __str__(self):
        return (
            f"MapVisualizationMsg(\n"
            f"    in_nodes = {self.in_nodes},\n"
            f"    out_nodes = {self.out_nodes},\n"
            f"    io_values = {self.io_values},\n"
            f"    concrete_nodes = {self.concrete_nodes},\n"
            f"    abstract_nodes = {self.abstract_nodes},\n"
            f"    forward_links = {self.forward_links},\n"
            f"    backward_links = {self.backward_links},\n"
            f"    forward_then = {self.forward_then},\n"
            f"    backward_then = {self.backward_then}\n"
            f")"
        )

    def __repr__(self):
        return self.__str__()
