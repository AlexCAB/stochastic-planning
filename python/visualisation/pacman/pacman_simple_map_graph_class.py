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
from typing import Optional

from websocket import WebSocketApp

from planning_engine.model.map_visualization_msg import MapVisualizationMsg


class PacmanSimpleMapGraph:

    def __init__(self):
        self.logger = logging.getLogger(self.__class__.__name__)
        self. ws_app: Optional[WebSocketApp] = None

    def on_ws_open(self, ws_app: WebSocketApp) -> None:
        self.ws_app = ws_app
        self.logger.info("WebSocket connection opened.")

    def on_ws_message(self, ws_app: WebSocketApp, data: MapVisualizationMsg) -> None:
        self.logger.info(f"Received message: {data}")

