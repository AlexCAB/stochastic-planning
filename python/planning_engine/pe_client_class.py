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
| created: 2025-07-16 ||||||||||"""

import logging
from typing import Dict

import requests

from planning_engine.pe_client_conf_class import PeClientConf


class PeClient:
    PATH_HEALTH = "/pe/v1/maintenance/__health"
    PATH_EXIT = "/pe/v1/maintenance/__exit"

    def _check_response(self, response: requests.Response, methode: str, request_path: str):
        if response.status_code != 200:
            raise ConnectionError(f"Failed to run {methode} {request_path}. "
                                  f"Status code: {response.status_code}, Response: {response.text}")

    def _run_get(self, request_path: str) -> Dict:
        response = requests.get(self.base_url + request_path)
        self._check_response(response, "GET", request_path)
        return response.json()

    def _run_post(self, request_path: str, data: Dict) -> Dict:
        response = requests.post(self.base_url + request_path, json=data)
        self._check_response(response, "POST", request_path)
        return response.json()

    def __init__(self, config: PeClientConf):
        self.logger = logging.getLogger(self.__class__.__name__)
        self.base_url: str = config.url

        pe_status = self._run_get(PeClient.PATH_HEALTH)
        self.logger.info(f"Connecting to planning engine at {self.base_url}, status: {pe_status}")

    def kill_planning_engine(self):
        response = requests.post(self.base_url + PeClient.PATH_EXIT)
        self._check_response(response, "POST", PeClient.PATH_EXIT)
        self.logger.info(f"Exit signal sent to planning engine, response: {self.base_url}}")
