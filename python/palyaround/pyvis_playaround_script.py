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
| created: 2025-12-26 ||||||||||"""

# python
import os
import webbrowser
from pyvis.network import Network


def main():
    out_dir = "palyaround"
    out_file = os.path.join(out_dir, "pyvis_playaround.html")
    os.makedirs(out_dir, exist_ok=True)

    net = Network(height="600px", width="100%", bgcolor="#222222", font_color="white")

    # add nodes (id, label, hover title, size via `value`, color)
    net.add_node(1, label="Node A", title="Group x", value=20, color="#e74c3c")
    net.add_node(2, label="Node B", title="Group y", value=30, color="#3498db")
    net.add_node(3, label="Node C", title="Group x", value=15, color="#2ecc71")

    # add edges (source, target, optional weight via `value`)
    net.add_edge(1, 2, value=1)
    net.add_edge(2, 3, value=2)
    net.add_edge(3, 1, value=3)
    net.add_edge(1, 3, value=1)

    # optional physics layout
    net.toggle_physics(True)

    print(f"Saved and opened: {net}")
    print(f"Saved and opened: {out_file}")
    # write and open
    net.show(out_file)
    # ensure file opens on systems where show may not auto-open
    webbrowser.open("file://" + os.path.abspath(out_file))










if __name__ == "__main__":
    main()
