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

import unittest

from planning_engine.model.map_info_class import MapInfo


class TestMapInfo(unittest.TestCase):
    def test_creates_instance_with_valid_parameters(self):
        map_info = MapInfo(
            db_name="db-name",
            map_name="TestMap",
            num_input_nodes=5,
            num_output_nodes=3,
            num_hidden_nodes=2
        )
        self.assertEqual(map_info.db_name, "db-name")
        self.assertEqual(map_info.map_name, "TestMap")
        self.assertEqual(map_info.num_input_nodes, 5)
        self.assertEqual(map_info.num_output_nodes, 3)
        self.assertEqual(map_info.num_hidden_nodes, 2)

    def test_raises_error_when_map_name_is_empty(self):
        with self.assertRaises(AssertionError):
            MapInfo(
                db_name="",
                map_name="TestMap",
                num_input_nodes=5,
                num_output_nodes=3,
                num_hidden_nodes=2
            )

    def test_converts_to_string_correctly(self):
        map_info = MapInfo(
            db_name="db-name",
            map_name="TestMap",
            num_input_nodes=5,
            num_output_nodes=3,
            num_hidden_nodes=2
        )
        self.assertEqual(
            str(map_info),
            "MapInfo(db_name=db-name, map_name=TestMap, num_input_nodes=5, num_output_nodes=3, num_hidden_nodes=2)"
        )
