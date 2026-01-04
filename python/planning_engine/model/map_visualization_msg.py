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

import json

from typing import Dict, Any, Set
from networkx import DiGraph


class MapVisualizationMsg:

    @staticmethod
    def from_raw_json(json_str: str) -> 'MapVisualizationMsg':
        assert json_str, "JSON data should not be empty"

        json_data = json.loads(json_str)

        assert 'inNodes' in json_data, "Input nodes should be defined in JSON data"
        assert 'outNodes' in json_data, "Output nodes should be defined in JSON data"
        assert 'ioValues' in json_data, "IO values should be defined in JSON data"
        assert 'concreteNodes' in json_data, "Concrete nodes should be defined in JSON data"
        assert 'abstractNodes' in json_data, "Abstract nodes should be defined in JSON data"
        assert 'linkEdges' in json_data, "Link edges should be defined in JSON data"
        assert 'thenEdges' in json_data, "Then edges should be defined in JSON data"

        return MapVisualizationMsg(
            in_nodes=set(json_data['inNodes']),
            out_nodes=set(json_data['outNodes']),
            io_values={str(e[0]): set(e[1]) for e in json_data['ioValues']},
            concrete_nodes=set(json_data['concreteNodes']),
            abstract_nodes=set(json_data['abstractNodes']),
            link_edges={int(e[0]): set(e[1]) for e in json_data['linkEdges']},
            then_edges={int(e[0]): set(e[1]) for e in json_data['thenEdges']}
        )

    @staticmethod
    def empty() -> 'MapVisualizationMsg':
        return MapVisualizationMsg(
            in_nodes=set(),
            out_nodes=set(),
            io_values=dict(),
            concrete_nodes=set(),
            abstract_nodes=set(),
            link_edges=dict(),
            then_edges=dict()
        )

    def __init__(
            self,
            in_nodes: Set[str],
            out_nodes: Set[str],
            io_values: Dict[str, Set[int]],
            concrete_nodes: Set[int],
            abstract_nodes: Set[int],
            link_edges: Dict[int, Set[int]],
            then_edges: Dict[int, Set[int]]):
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
        assert link_edges is not None, "Link edges should be defined"
        assert isinstance(link_edges, dict), "Link edges  should be a dictionary"
        assert then_edges is not None, "Then edges should be defined"
        assert isinstance(then_edges, dict), "Then edges should be a dictionary"

        self.in_nodes: Set[str] = in_nodes
        self.out_nodes: Set[str] = out_nodes
        self.io_values: Dict[str, Set[int]] = io_values
        self.concrete_nodes: Set[int] = concrete_nodes
        self.abstract_nodes: Set[int] = abstract_nodes
        self.link_edges: Dict[int, Set[int]] = link_edges
        self.then_edges: Dict[int, Set[int]] = then_edges

    def __eq__(self, other: Any) -> bool:
        return isinstance(other, MapVisualizationMsg) \
            and self.in_nodes == other.in_nodes \
            and self.out_nodes == other.out_nodes \
            and self.io_values == other.io_values \
            and self.concrete_nodes == other.concrete_nodes \
            and self.abstract_nodes == other.abstract_nodes \
            and self.link_edges == other.link_edges \
            and self.then_edges == other.then_edges

    def __str__(self):
        return (
            f"MapVisualizationMsg(\n"
            f"    in_nodes = {self.in_nodes},\n"
            f"    out_nodes = {self.out_nodes},\n"
            f"    io_values = {self.io_values},\n"
            f"    concrete_nodes = {self.concrete_nodes},\n"
            f"    abstract_nodes = {self.abstract_nodes},\n"
            f"    link_edges = {self.link_edges},\n"
            f"    then_edges = {self.then_edges}\n"
            f")"
        )

    def to_graph(self, graph: DiGraph) -> DiGraph:
        graph.clear()

        for name in self.in_nodes:
            graph.add_node(name,  color="green", label=f"IN:{name}")

        for name in self.out_nodes:
            graph.add_node(name, color="red", label=f"OUT:{name}")

        for i in self.concrete_nodes:
            graph.add_node(i, color="blue", label=f"CON:{str(i)}")

        for i in self.abstract_nodes:
            graph.add_node(i, color="gray", label=f"ABS:{str(i)}")

        for s, ts in self.io_values.items():
            for t in ts:
                graph.add_edge(s, t, color="gray")

        for s, ts in self.link_edges.items():
            for t in ts:
                graph.add_edge(s, t, color="red")

        for s, ts in self.then_edges.items():
            for t in ts:
                graph.add_edge(s, t, color="green")

        return graph

    def __repr__(self):
        return self.__str__()
