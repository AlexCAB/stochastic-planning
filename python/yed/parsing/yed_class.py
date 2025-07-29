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
| created: 2025-07-29 ||||||||||"""

import logging
import os
from typing import List, Dict, Set

from bs4 import BeautifulSoup
from networkx.algorithms.components import weakly_connected_components
from networkx.classes import MultiDiGraph
from networkx.readwrite.graphml import read_graphml

from planning_engine.model.io_node_class import IoNode
from planning_engine.model.map_definition_class import MapDefinition
from yed.parsing.parsed_edge_class import ParsedEdge
from yed.parsing.parsed_node_class import ParsedNode


class Yed:
    def __init__(self, yed_path: str):
        self.logger = logging.getLogger(self.__class__.__name__)

        assert yed_path, "Yed path should be defined"
        self.logger.info(f"[load_yed_graph] Loading yEd graph from {yed_path}, work dir = {os.getcwd()}")

        self.graph: MultiDiGraph = read_graphml(yed_path)

        with (open(yed_path, encoding="utf8") as fp):
            self.nodes: Dict[str, ParsedNode] = \
                {id: ParsedNode(id, props['label']) for (id, props) in self.graph.nodes(data=True)}

            self.edges: List[ParsedEdge] = [
                ParsedEdge(e) for e in BeautifulSoup(fp, features="xml").find_all("edge")]

            self.components: List[Set[str]] = list(weakly_connected_components(self.graph))

        self.logger.info(f"Parsed {len(self.nodes)} nodes from yEd graph, at {yed_path}")
        self.logger.info(f"Parsed {len(self.edges)} edges from yEd graph, at {yed_path}")
        self.logger.info(f"Found {len(self.components)} weakly connected components in yEd graph, at {yed_path}")

    def build_map_definition(self, db_name: str) -> MapDefinition:
        graph_node: ParsedNode = next(filter(lambda n: n.node_type == "G", self.nodes.values()), None)
        assert graph_node is not None, "Graph node should be defined"

        component: Set[str] = next((c for c in self.components if graph_node.id in c), None)
        assert component is not None, "Graph node should be in one of the components"

        in_nodes: List[ParsedNode] = [self.nodes[id] for id in component if self.nodes[id].is_input_node()]
        assert len(in_nodes) > 0, "At least one input node should be defined"

        out_nodes: List[ParsedNode] = [self.nodes[id] for id in component if self.nodes[id].is_output_node()]
        assert len(out_nodes) > 0, "At least one output node should be defined"

        definition: MapDefinition = MapDefinition(
            db_name=db_name,
            name=graph_node.name,
            description=graph_node.description,
            input_nodes=[IoNode.create(n.name, n.value_type, n.value_range) for n in in_nodes],
            output_nodes=[IoNode.create(n.name, n.value_type, n.value_range) for n in out_nodes]
        )

        self.logger.info(f"Built MapDefinition from yEd graph: {definition}")

        return definition
