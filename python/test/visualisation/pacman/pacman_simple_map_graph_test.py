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
| created: 2026-01-04 ||||||||||"""

import logging
import unittest
import copy
import time

from networkx import DiGraph

from planning_engine.model.map_visualization_msg import MapVisualizationMsg
from visualisation.pacman.pacman_simple_map_graph_class import PacmanSimpleMapGraph

logging.basicConfig(level=logging.INFO)


class TestPacmanSimpleMapGraph(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestPacmanSimpleMapGraph, self).__init__(*args, **kwargs)
        self.logger = logging.getLogger(self.__class__.__name__)

    def test_run_with_simple_graphs(self):
        msg_1 = MapVisualizationMsg.empty()
        self.logger.info(f"Created msg_1 graph: {msg_1.to_graph(DiGraph()).nodes(data=True)}")

        msg_2 = copy.copy(msg_1)
        msg_2.in_nodes = {"in-node"}
        msg_2.out_nodes = {"out-node"}
        self.logger.info(f"Created msg_2 graph: {msg_2.to_graph(DiGraph()).nodes(data=True)}")

        msg_3 = copy.copy(msg_2)
        msg_3.concrete_nodes = {1, 2, 3}
        msg_3.abstract_nodes = {4, 5}
        self.logger.info(f"Created msg_3 graph: {msg_3.to_graph(DiGraph()).nodes(data=True)}")

        msg_4 = copy.copy(msg_3)
        msg_4.io_values = {"in-node": {1, 3}, "out-node": {2}}
        self.logger.info(f"Created msg_4 graph: {msg_4.to_graph(DiGraph()).edges(data=True)}")

        msg_5 = copy.copy(msg_4)
        msg_5.link_edges = {1: {4, 5}, 2: {5}, 4: {5}}
        msg_5.then_edges = {4: {5}, 5: {4}}
        self.logger.info(f"Created msg_5 graph: {msg_5.to_graph(DiGraph()).edges(data=True)}")

        graph = PacmanSimpleMapGraph()

        graph.on_ws_open(None)
        time.sleep(1)
        graph.on_ws_message(None, msg_1)
        time.sleep(1)
        graph.on_ws_message(None, msg_2)
        time.sleep(1)
        graph.on_ws_message(None, msg_3)
        time.sleep(1)
        graph.on_ws_message(None, msg_4)
        time.sleep(1)
        graph.on_ws_message(None, msg_5)
        time.sleep(5)


if __name__ == '__main__':
    unittest.main()
