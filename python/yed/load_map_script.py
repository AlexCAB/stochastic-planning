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
| created:  2024-12-26 |||||||||"""

import argparse
import logging

from planning_engine.config.pe_client_conf_class import PeClientConf
from planning_engine.pe_client_class import PeClient
from yed.parsing.yed_class import Yed

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def read_args():
    parser = argparse.ArgumentParser(description="yEd map graph loader")
    parser.add_argument(
        '--path',
        type=str,
        required=True,
        help="Path to yEd graph file (*.graphml), "
             "example: --path 'yed/example_maps/pacman-5x3-two-ways.graphml'")

    parser.add_argument(
        '--database',
        type=str,
        required=True,
        help="Name of the database where yEd graph file (*.graphml) will be loaded, "
             "example: --database pacman-5x3-two-ways")

    parser.add_argument(
        '--pe_config',
        type=str,
        required=True,
        help="Path to the plunning engine configuration file, "
             "example: --pe_config config/pe_client_config.json")

    args = parser.parse_args()
    return args#             knowledge_graph.add_sample(full_sample)


def main():
    args = read_args()
    yed = Yed(args.path)

    map_definition = yed.build_map_definition(args.database)
    samples = yed.build_samples()

    client = PeClient(PeClientConf.from_json_file(args.pe_config))

    client.reset_map()
    client.init_map(map_definition)
    client.add_samples(samples)


if __name__ == "__main__":
    main()
