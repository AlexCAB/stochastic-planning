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

from planning_engine.model.hidden_edge_class import HiddenEdge, EdgeType
from planning_engine.model.hidden_node_class import HiddenNode, AbstractNode, ConcreteNode
from planning_engine.model.io_node_class import IoNode
from planning_engine.model.map_definition_class import MapDefinition
from planning_engine.model.sample_class import Sample, Samples
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

    def _build_sample(self, sample_node: ParsedNode) -> Sample:
        assert sample_node.is_sample_node(), f"Node {sample_node} is not a sample node"

        component: Set[str] = next(iter([c for c in self.components if sample_node.id in c]), None)

        sample_nodes: Dict[str, ParsedNode] = {
            id: node for id, node in self.nodes.items()
            if id in component and id != sample_node.id
        }

        found_keys: Set[str] = set(sample_nodes.keys()) | {sample_node.id}
        assert found_keys == component, f"Not all sample nodes found, found: {found_keys}, required: {component}"

        sample_edges: List[ParsedEdge] = [
            e for e in self.edges
            if (e.source_id in sample_nodes or e.target_id in sample_nodes) and not e.is_support_edge()
        ]

        edge_end_not_in_component: Set[ParsedEdge] = {
            e for e in sample_edges
            if e.source_id not in sample_nodes or e.target_id not in sample_nodes
        }

        assert not edge_end_not_in_component, f"Edges with end not in component found: {edge_end_not_in_component}"

        assert all([(n.is_abstract_node() or n.is_concrete_node()) for n in sample_nodes.values()]), \
            "All nodes in the sample should be abstract or concrete nodes"

        assert all([(e.is_link_edge() or e.is_then_edge()) for e in sample_edges]), \
            "All edges in the sample should be link or then edges"

        hidden_edges: List[HiddenEdge] = [
            HiddenEdge(
                source_hn_name=sample_nodes[e.source_id].name,
                target_hn_name=sample_nodes[e.target_id].name,
                edge_type=EdgeType.LinkEdge if e.is_link_edge() else EdgeType.ThenEdge)
            for e in sample_edges
        ]

        sample: Sample = Sample(
            probability_count=sample_node.probability_count,
            utility=sample_node.utility,
            name=sample_node.name,
            description=sample_node.description,
            edges=hidden_edges
        )

        return sample

    def build_samples(self) -> Samples:
        hidden_nodes: Dict[str, HiddenNode] = {}

        for n in self.nodes.values():
            if n.is_abstract_node() or n.is_concrete_node():
                hn = AbstractNode(n.name, n.description) if n.is_abstract_node() else \
                    ConcreteNode(n.name, n.description, n.variable_name, n.value)
                if n.name in hidden_nodes:
                    assert hidden_nodes[n.name] == hn, \
                        f"Node {n.name} already exists with different params: " \
                        f"{hidden_nodes[n.name]} != {n}"
                    if hn.description and hidden_nodes[n.name].description is None:
                        hidden_nodes[n.name].description = hn.description
                else:
                    hidden_nodes[n.name] = hn

        samples: Samples = Samples(
            list(hidden_nodes.values()),
            [self._build_sample(n) for n in self.nodes.values() if n.is_sample_node()]
        )

        self.logger.info(f"Built Samples from yEd graph: {samples}")
        return samples
