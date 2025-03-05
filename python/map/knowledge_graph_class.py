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
| created:  2024-12-19 |||||||||"""
from typing import FrozenSet

from map.full_sample_graph_class import *
from map.hidden_node_class import *
from map.io_node_class import *

if TYPE_CHECKING:
    from database.neo4j_database_class import No4jDatabase


class Metadata:
    def __init__(self, name: str,  sample_data: List[SampleData], description: str | None = None):
        assert name is not None, "Name should be defined"
        assert sample_data is not None, "Sample data should be defined"

        self.name: str = name
        self.sample_data: Dict[int, SampleData] = {data.sample_id: data for data in sample_data}
        self.description: str | None = description

    def __str__(self):
        return (f"Metadata: {self.name}\n"
                f"Description: {self.description}\n"
                f"Number of samples: {len(self.sample_data)}\n")

    def to_properties(self) -> Dict[str, Any]:
        return {
            "name": self.name,
            "num_of_samples": len(self.sample_data),
            "description": self.description
        }


class KnowledgeGraph:
    @staticmethod
    def create_empty(
            name: str,
            input_nodes: List[InputNode],
            output_nodes: List[OutputNode],
            database: 'No4jDatabase',
            description: str | None = None) -> 'KnowledgeGraph':

        metadata: Metadata = Metadata(name, [], description)
        database.clear_database()
        root_neo4j_id: str = database.insert_root_node(metadata, input_nodes, output_nodes)
        return KnowledgeGraph(metadata, input_nodes, output_nodes, root_neo4j_id, hidden = None, relations = None)

    def __init__(
            self,
            metadata: Metadata,
            input_nodes: List[InputNode],
            output_nodes: List[OutputNode],
            root_neo4j_id: str,
            hidden: List[HiddenNode] = None,
            relations: List[HiddenEdge] = None):

        assert metadata is not None, "Metadata should be defined"
        assert input_nodes is not None, "Input should be defined"
        assert output_nodes is not None, "Output should be defined"
        assert root_neo4j_id is not None and root_neo4j_id != "", "Root Neo4j id should be defined"

        self.metadata: Metadata = metadata
        self.input: List[InputNode] = input_nodes
        self.output: List[OutputNode] = output_nodes
        self.root_neo4j_id: str = root_neo4j_id

        self.nodes_cache: Dict[str, HiddenNode] = {} # Dict[neo4j_id, node]
        self.edges_cache: Dict[FrozenSet[str], HiddenEdge] = {} # Dict[FrozenSet[source_neo4j_id, target_neo4j_id], edge]

        for node in hidden if hidden else []:
            assert node.neo4j_id, f"Neo4j id should be defined for hidden node{node}"
            match node:
                case n if isinstance(n, AbstractHiddenNode) or isinstance(n, ConcreteHiddenNode):
                    self.nodes_cache[node.neo4j_id] = n
                case n:
                    raise ValueError(f"Unknown hidden node type: {n}, should be either abstract or concrete")

        for edge in relations if relations else []:
            assert edge.source.neo4j_id and edge.target.neo4j_id, \
                f"Source and target nodes Neo4j ids should be defined for edge{edge}"

            match edge:
                case e if isinstance(e, LinkHiddenEdge) or isinstance(e, ThenHiddenEdge):
                    self.edges_cache[frozenset([edge.source.neo4j_id, edge.target.neo4j_id])] = e
                case e:
                    raise ValueError(f"Unknown hidden edge type: {e}, should be either link or then")

    def get_variable_nodes_for_name(self, name: str) -> List[HiddenNode]:
        return [node for node in self.nodes_cache.values() if node.name == name]

    def get_io_nodes_for_name(self, name: str) -> List[IoNode]:
        return [node for node in (self.input + self.output) if node.name == name]

    def add_sample(self, sample: FullSampleGraph):
        neo4j_ids_to_load: List[str] = []
        for value_node in sample.value_nodes:


            value_node

            if n.hidden_node:
                neo4j_ids_to_load.append(n.hidden_node.neo4j_id)


        self._load_in_cache([n fo n sample.value_nodes])





    def __str__(self):
        return (f"Knowledge graph: '{self.metadata.name}'\n"
                f"Description: {self.metadata.description}\n"
                f"Root Neo4j id: {self.root_neo4j_id}\n"
                f"Input nodes:\n  {"\n  ".join([str(n) for n in self.input])}\n"
                f"Output nodes:\n  {"\n  ".join([str(n) for n in self.output])}")

    def __repr__(self):
        return self.__str__()

    def _load_in_cache(self, neo4j_ids: List[str]):
        # Do nit load nodes and edges that are already in cache
        pass
