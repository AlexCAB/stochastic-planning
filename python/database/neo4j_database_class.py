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
from neo4j import GraphDatabase


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




