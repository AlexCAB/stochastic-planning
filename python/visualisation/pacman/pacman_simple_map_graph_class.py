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
| created: 2026-01-02 ||||||||||"""

import logging
import networkx as nx
import matplotlib.pyplot as plt

from typing import Optional

from networkx import DiGraph
from websocket import WebSocketApp
from planning_engine.model.map_visualization_msg import MapVisualizationMsg


class PacmanSimpleMapGraph:

    def __init__(self):
        self.logger = logging.getLogger(self.__class__.__name__)
        self.ws_app: Optional[WebSocketApp] = None
        self.graph: DiGraph = nx.DiGraph()
        self.fig, self.ax = plt.subplots(figsize=(8, 6))

    def _draw_graph(self) -> None:
        self.ax.clear()
        pos = nx.spring_layout(self.graph, seed=42)

        node_color = [n[1]['color'] for n in self.graph.nodes(data=True)]
        edge_color = [e[2]['color'] for e in self.graph.edges(data=True)]
        labels = {n[0]: n[1]['label'] for n in self.graph.nodes(data=True)}

        nx.draw(
            self.graph, pos, ax=self.ax,
            with_labels=False,
            node_color=node_color,
            node_size=800,
            edge_color=edge_color
        )

        nx.draw_networkx_labels(
            self.graph, pos, labels,
            font_size=8,
            font_color="black"
        )

        plt.pause(0.1)  # Pause to update the plot

    def on_ws_open(self, ws_app: WebSocketApp) -> None:
        self.ws_app = ws_app
        self.logger.info("WebSocket connection opened.")
        self._draw_graph()

    def on_ws_message(self, ws_app: WebSocketApp, data: MapVisualizationMsg) -> None:
        self.logger.info(f"Received message: {data}")
        data.to_graph(self.graph)
        self._draw_graph()
