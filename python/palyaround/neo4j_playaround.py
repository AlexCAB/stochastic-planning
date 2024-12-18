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
| created:  2024-11-30 |||||||||"""

import json
from neo4j import GraphDatabase


def main():
    print("########### TEST ###########")

    driver_config = json.load(open("../config/neo4j_driver.json"))

    print(driver_config)

    uri = driver_config['uri']
    auth = (driver_config['username'], driver_config['password'])
    db_name = "playaround"

    with GraphDatabase.driver(uri, auth=auth) as driver:
        driver.verify_connectivity()

        with driver.session(database=db_name) as session:

            # res = session.run("CREATE (a:A {name: 'node_a'})-[r:R {name: 'edge_r'}]->(b:B {name: 'node_b'})")

            res = session.run("MATCH (a:A)-[r:R]->(b) RETURN a, r, b, elementId(a)")

            json_data = json.dumps(res.data())

            print(f"res = {json_data}")
























if __name__ == "__main__":
    main()
