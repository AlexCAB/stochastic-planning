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
import os
import json
from typing import List, Dict, Set, Tuple

from networkx.algorithms.components import weakly_connected_components
from networkx.classes import MultiDiGraph
from networkx.readwrite.graphml import read_graphml

from database.neo4j_database_class import No4jDatabase
from map.io_node_class import InputNode, OutputNode
from map.io_variable_class import IoVariable
from map.knowledge_graph_class import KnowledgeGraph
from yed.parsed_node_class import ParsedNode

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def read_args():
    parser = argparse.ArgumentParser(description="yEd map graph loader")
    parser.add_argument(
        '--path',
        type=str,
        required=False,
        default="yed/example_maps/pacman-5x3-two-ways.graphml",
        help="Path to yEd graph file (*.graphml)")

    parser.add_argument(
        '--database',
        type=str,
        required=False,
        default="pacman-5x3-two-ways",
        help="Name of the database where yEd graph file (*.graphml) will be loaded")

    parser.add_argument(
        '--neo4j_config',
        type=str,
        required=False,
        default="config/neo4j_driver.json",
        help="Path to the Neo4j driver configuration file")

    args = parser.parse_args()
    return args


def read_and_parse_yed_file(yed_path: str) -> Tuple[Dict[str, ParsedNode], List[Set[str]]]:
    yed_graph: MultiDiGraph = read_graphml(yed_path)

    parsed_nodes: Dict[str, ParsedNode] = {
        id: ParsedNode(id, props['label']) for (id, props) in yed_graph.nodes(data=True)}

    graph_components: List[Set[str]] = list(weakly_connected_components(yed_graph))
    return parsed_nodes, graph_components


def connect_to_neo4j(driver_config_path: str, database_name: str) -> No4jDatabase:
    driver_config = json.load(open(driver_config_path))
    return No4jDatabase(
        driver_config['uri'],
        driver_config['username'],
        driver_config['password'],
        database_name)


def build_empty_knowledge_graph(
        parsed_nodes: Dict[str, ParsedNode],
        graph_components: List[Set[str]],
        database: No4jDatabase) -> KnowledgeGraph:

    graph_node: ParsedNode = next(filter(lambda n: n.node_type == "G", parsed_nodes.values()), None)
    assert graph_node is not None, "Graph node should be defined"
    component: Set[str] = next((c for c in graph_components if graph_node.id in c), None)
    assert component is not None, "Graph node should be in one of the components"

    in_parsed_nodes: List[ParsedNode] = [parsed_nodes[id] for id in component if parsed_nodes[id].node_type == "I"]
    assert len(in_parsed_nodes) > 0, "At least one input node should be defined"
    out_parsed_nodes: List[ParsedNode] = [parsed_nodes[id] for id in component if parsed_nodes[id].node_type == "O"]
    assert len(out_parsed_nodes) > 0, "At least one output node should be defined"

    input_nodes: List[InputNode] = [
        InputNode(node.name, IoVariable.create_variable(node.value_type, node.value_range))
        for node in in_parsed_nodes]

    output_nodes: List[OutputNode] = [
        OutputNode(node.name, IoVariable.create_variable(node.value_type, node.value_range))
        for node in out_parsed_nodes]

    names: List[str] = [n.name for n in input_nodes + output_nodes]
    assert len(set(names)) == len(names), f"IO node names should be unique, names = {names}"

    return KnowledgeGraph.create_empty_knowledge_graph(
        graph_node.name,
        input_nodes,
        output_nodes,
        database,
        graph_node.description)








def load_yed_graph(yed_path: str, database_name: str, neo4j_config_path: str):
    logger.info(f"[load_yed_graph] Loading yEd graph from {yed_path}, work dir = {os.getcwd()}")

    parsed_nodes, graph_components = read_and_parse_yed_file(yed_path)

    for node in parsed_nodes.values():
        logger.info(f"[load_yed_graph] Found yEd {node}")

    # TODO: Preparing as samples

    with connect_to_neo4j(neo4j_config_path, database_name) as database:
        logger.info(f"[load_yed_graph] Connected to Neo4j database: {database}")

        knowledge_graph: KnowledgeGraph = build_empty_knowledge_graph(parsed_nodes, graph_components, database)
        logger.info(f"[load_yed_graph] Created empty: {knowledge_graph}")










def main():
    args = read_args()
    load_yed_graph(args.path, args.database, args.neo4j_config)


if __name__ == "__main__":
    main()
