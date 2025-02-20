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

from map.full_sample_graph_class import *
from map.hidden_node_class import *
from map.io_node_class import *

if TYPE_CHECKING:
    from database.neo4j_database_class import No4jDatabase


class Metadata:
    def __init__(self, name: str, num_of_samples: int, sample_data: List[SampleData], description: str | None = None):
        assert name is not None, "Name should be defined"
        assert num_of_samples is not None, "Number of samples should be defined"
        assert sample_data is not None, "Sample data should be defined"

        self.name: str = name
        self.num_of_samples: int = num_of_samples
        self.sample_data: Dict[int, SampleData] = {data.sample_id: data for data in sample_data}
        self.description: str | None = description

    def __str__(self):
        return (f"Metadata: {self.name}\n"
                f"Description: {self.description}\n"
                f"Number of samples: {self.num_of_samples}\n")

    def to_properties(self) -> Dict[str, Any]:
        return {
            "name": self.name,
            "num_of_samples": self.num_of_samples,
            "description": self.description
        }


class KnowledgeGraph:
    @staticmethod
    def create_empty_knowledge_graph(
            name: str,
            input_nodes: List[InputNode],
            output_nodes: List[OutputNode],
            database: 'No4jDatabase',
            description: str | None = None) -> 'KnowledgeGraph':

        metadata: Metadata = Metadata(name, 0, [], description)
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
            relations: List[RelationEdge] = None):

        assert metadata is not None, "Metadata should be defined"
        assert input_nodes is not None, "Input should be defined"
        assert output_nodes is not None, "Output should be defined"
        assert root_neo4j_id is not None and root_neo4j_id != "", "Root Neo4j id should be defined"

        self.metadata: Metadata = metadata
        self.input: List[InputNode] = input_nodes
        self.output: List[OutputNode] = output_nodes
        self.root_neo4j_id: str = root_neo4j_id
        self.hidden: List[HiddenNode] = hidden if hidden is not None else []
        self.relations: List[RelationEdge] = relations if relations is not None else []

    def __str__(self):
        return (f"Knowledge graph: '{self.metadata.name}'\n"
                f"Description: {self.metadata.description}\n"
                f"Root Neo4j id: {self.root_neo4j_id}\n"
                f"Input nodes:\n  {"\n  ".join([str(n) for n in self.input])}\n"
                f"Output nodes:\n  {"\n  ".join([str(n) for n in self.output])}")
