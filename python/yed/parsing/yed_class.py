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
import warnings
from typing import List, Dict, Set

from bs4 import BeautifulSoup, XMLParsedAsHTMLWarning
from networkx.algorithms.components import weakly_connected_components
from networkx.classes import MultiDiGraph
from networkx.readwrite.graphml import read_graphml

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
