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

import argparse
import logging

from planning_engine.config.pe_client_conf_class import PeClientConf
from planning_engine.pe_client_class import PeClient
from visualisation.pacman.pacman_simple_map_graph_class import PacmanSimpleMapGraph

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def read_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Pacman simple map visualisation script")

    parser.add_argument(
        '--pe_config',
        type=str,
        required=True,
        help="Path to the planning engine configuration file, "
             "example: --pe_config config/pe_client_config.json")

    args = parser.parse_args()
    return args


def main():
    args: argparse.Namespace = read_args()
    logger.info(f"Starting with args: {args}")

    client: PeClient = PeClient(PeClientConf.from_json_file(args.pe_config))
    graph: PacmanSimpleMapGraph = PacmanSimpleMapGraph()

    client \
        .build_map_visualisation_ws_app(graph.on_ws_open, graph.on_ws_message) \
        .run_forever()


if __name__ == "__main__":
    main()
