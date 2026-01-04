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
| created: 2026-01-01 ||||||||||"""

import unittest
import networkx as nx

from planning_engine.model.map_visualization_msg import MapVisualizationMsg
from networkx import DiGraph


class TestMapVisualizationMsg(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestMapVisualizationMsg, self).__init__(*args, **kwargs)

        self.empty_raw_json = (
            '{'
            '  "inNodes": [],'
            '  "outNodes": [],'
            '  "ioValues": [],'
            '  "concreteNodes": [],'
            '  "abstractNodes": [],'
            '  "linkEdges": [],'
            '  "thenEdges": []'
            '}')

        self.filled_raw_json = (
            '{'
            '  "inNodes": ["Input1", "Input2"],'
            '  "outNodes": ["Output1"],'
            '  "ioValues": [["Input1", [1, 2]], ["Output1", [3]]],'
            '  "concreteNodes": [1, 2, 3],'
            '  "abstractNodes": [4, 5],'
            '  "linkEdges": [[1, [2, 4]], [2, [3]]],'
            '  "thenEdges": [[3, [2]], [2, [1, 5]]]'
            '}')

        self.msg = MapVisualizationMsg(
            in_nodes={"Input1", "Input2"},
            out_nodes={"Output1"},
            io_values={"Input1": {1, 2}, "Output1": {3}},
            concrete_nodes={1, 2, 3},
            abstract_nodes={4, 5},
            link_edges={1: {2, 4}, 2: {3}},
            then_edges={3: {2}, 2: {1, 5}}
        )

    def validate(self, msg):
        self.assertEqual(msg.in_nodes, {"Input1", "Input2"})
        self.assertEqual(msg.out_nodes, {"Output1"})
        self.assertEqual(msg.io_values, {"Input1": {1, 2}, "Output1": {3}})
        self.assertEqual(msg.concrete_nodes, {1, 2, 3})
        self.assertEqual(msg.abstract_nodes, {4, 5})
        self.assertEqual(msg.link_edges, {1: {2, 4}, 2: {3}})
        self.assertEqual(msg.then_edges, {3: {2}, 2: {1, 5}})

    def test_creates_instance_with_valid_parameters(self):
        self.validate(self.msg)

    def test_from_empty_raw_json_with_valid_json(self):
        data = MapVisualizationMsg.from_raw_json(self.empty_raw_json)
        self.assertEqual(data.in_nodes, set([]))
        self.assertEqual(data.out_nodes, set([]))

    def test_from_filled_raw_json_with_valid_json(self):
        self.validate(MapVisualizationMsg.from_raw_json(self.filled_raw_json))

    def test_to_graph(self):
        msg: MapVisualizationMsg = self.msg
        graph: DiGraph = self.msg.to_graph(graph=nx.DiGraph())

        io_values = set([(s, t) for s, ts in msg.io_values.items() for t in ts])
        link_edges = set([(s, t) for s, ts in msg.link_edges.items() for t in ts])
        then_edges = set([(s, t) for s, ts in msg.then_edges.items() for t in ts])

        expected_nodes = msg.in_nodes | msg.out_nodes | msg.concrete_nodes | msg.abstract_nodes
        expected_edges = io_values | link_edges | then_edges

        self.assertEqual(set(graph.nodes), expected_nodes)
        self.assertEqual(set(graph.edges), expected_edges)


if __name__ == '__main__':
    unittest.main()
