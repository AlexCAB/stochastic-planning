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

from planning_engine.model.map_visualization_msg import MapVisualizationMsg


class TestMapVisualizationMsg(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestMapVisualizationMsg, self).__init__(*args, **kwargs)

        self.json_data = {
            "inNodes": ["Input1", "Input2"],
            "outNodes": ["Output1"],
            "ioValues": {"Input1": [1, 2], "Output1": [3]},
            "concreteNodes": [1, 2, 3],
            "abstractNodes": [4, 5],
            "forwardLinks": {"1": [2], "2": [3]},
            "backwardLinks": {"3": [2], "2": [1]},
            "forwardThen": {"1": [4], "2": [5]},
            "backwardThen": {"4": [1], "5": [2]}
        }

        self.msg = MapVisualizationMsg(
            in_nodes={"Input1", "Input2"},
            out_nodes={"Output1"},
            io_values={"Input1": {1, 2}, "Output1": {3}},
            concrete_nodes={1, 2, 3},
            abstract_nodes={4, 5},
            forward_links={1: {2}, 2: {3}},
            backward_links={3: {2}, 2: {1}},
            forward_then={1: {4}, 2: {5}},
            backward_then={4: {1}, 5: {2}}
        )

    def validate(self, msg):
        self.assertEqual(msg.in_nodes, {"Input1", "Input2"})
        self.assertEqual(msg.out_nodes, {"Output1"})
        self.assertEqual(msg.io_values, {"Input1": {1, 2}, "Output1": {3}})
        self.assertEqual(msg.concrete_nodes, {1, 2, 3})
        self.assertEqual(msg.abstract_nodes, {4, 5})
        self.assertEqual(msg.forward_links, {1: {2}, 2: {3}})
        self.assertEqual(msg.backward_links, {3: {2}, 2: {1}})
        self.assertEqual(msg.forward_then, {1: {4}, 2: {5}})
        self.assertEqual(msg.backward_then, {4: {1}, 5: {2}})

    def test_creates_instance_with_valid_parameters(self):
        self.validate(self.msg)

    def test_from_json_with_valid_json(self):
        self.validate(MapVisualizationMsg.from_json(self.json_data))

    def test_to_json(self):
        self.assertEqual(MapVisualizationMsg.from_json(self.msg.to_json()), self.msg)


if __name__ == '__main__':
    unittest.main()
