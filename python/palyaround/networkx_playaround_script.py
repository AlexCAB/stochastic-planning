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
| created: 2026-01-03 ||||||||||"""

import networkx as nx
import matplotlib.pyplot as plt
import random


def generate_random_3Dgraph(n_nodes, radius, seed=None):
    if seed is not None:
        random.seed(seed)

    # Generate a dict of positions
    pos = {i: (random.uniform(0, 1), random.uniform(0, 1), random.uniform(0, 1)) for i in range(n_nodes)}

    # Create random 3D network
    G = nx.random_geometric_graph(n_nodes, radius, pos=pos)

    return G


def main():
    print("#################### NetworkX Playaround ####################")

    plt.ion()

    n = 200
    G = generate_random_3Dgraph(n_nodes=n, radius=0.25, seed=1)
    print("G:", G)

    # Create a matplotlib figure
    fig, ax = plt.subplots(figsize=(6, 4))

    ax.clear()  # Clear previous frame
    pos = nx.spring_layout(G, seed=42)  # Layout for consistent positioning
    nx.draw(
        G, pos, ax=ax,
        with_labels=True,
        node_color='skyblue',
        node_size=800,
        font_size=10,
        edge_color='gray'
    )
    plt.draw()
    plt.pause(1.5)


if __name__ == "__main__":
    main()
