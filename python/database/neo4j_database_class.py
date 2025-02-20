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
| created: 2025-02-19 ||||||||||"""

from typing import List, Dict, Any

from neo4j import GraphDatabase

from map.io_node_class import InputNode, OutputNode
from map.knowledge_graph_class import Metadata


"""
This class is a wrapper for the Neo4j database. It is used to connect to the database, clear it, insert root node, etc.
Knowledge graph have two parts:
- root graph - have ROOT node, which contains the metadata of the graph and the input and output nodes definitions.
               Set of SAMPLE connected to the _ROOT_ node, which contains the sample data.
- hidden graph - contains the hidden nodes and edges, which are used to represent the knowledge map.
"""
class No4jDatabase:
    def __init__(self, uri: str, user: str, password: str, database_name: str):
        assert uri is not None, "URI should be defined"
        assert user is not None, "User should be defined"
        assert password is not None, "Password should be defined"
        assert database_name is not None, "Database name should be defined"

        self.uri: str = uri
        self.user: str = user
        self.password: str = password
        self.database_name: str = database_name


    def __enter__(self):
        self.driver = GraphDatabase.driver( self.uri, auth=( self.user,  self.password))

        with self.driver.session() as session:
            assert \
                session.run("RETURN 1").values()[0][0] == 1, \
                f"Connection to the database failed, uri = { self.uri}, user = { self.user}, " \
                "database_name = { self.database_name}"

        return self

    def __exit__(self, *args):
        self.driver.close()

    def __str__(self):
        return f"Database at: uri = {self.uri}, user = {self.user}, database_name = {self.database_name}"

    def close(self):
        self.driver.close()

    def clear_database(self):
        with self.driver.session() as session:
            session.run("MATCH (n) DETACH DELETE n")

    def insert_root_node(self, metadata: Metadata, input: List[InputNode], output: List[OutputNode]) -> str:
        def flat_properties(properties: Dict[str, Any], path: str) -> Dict[str, str]:
            result = {}
            for key, value in properties.items():
                if isinstance(value, dict):
                    result.update(flat_properties(value, f"{path}.{key}"))
                else:
                    result[f"{path}.{key}"] = str(value)
            return result

        properties = {
            "metadata": metadata.to_properties(),
            "input": {node.name: node.to_properties() for node in input},
            "output": {node.name: node.to_properties() for node in output}
        }

        with self.driver.session() as session:
            response = session.run(
                "CREATE (n:ROOT $properties) RETURN elementId(n)",
                properties=flat_properties(properties, "root"))

            return str(response.values()[0][0])
