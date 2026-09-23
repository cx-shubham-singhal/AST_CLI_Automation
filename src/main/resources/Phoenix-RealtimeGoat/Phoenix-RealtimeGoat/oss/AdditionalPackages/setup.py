#!/usr/bin/env python3
# TC_38: setup.py detection — PackageManager = 'pypi'
# TC_39: install_requires parsing
# TC_40: extras_require parsing
# TC_41: tests_require parsing
from setuptools import setup, find_packages

setup(
    name='vuln-python-app',
    version='1.0.0',
    description='Vulnerable Python application for SCA testing',
    author='Test Team',
    packages=find_packages(),
    python_requires='>=3.8',

    # TC_39: install_requires — all packages with exact versions scanned
    install_requires=[
        'Flask==2.0.1',             # CVE-2023-30861
        'Django==3.2.4',            # CVE-2021-33203
        'SQLAlchemy==1.4.22',       # CVE-2019-7548
        'requests==2.27.1',         # CVE-2023-32681
        'PyYAML==5.3.1',            # CVE-2020-14343
        'cryptography==3.3.1',      # CVE-2023-49083
        'paramiko==2.7.2',          # CVE-2022-24302
        'Pillow==8.3.1',            # CVE-2021-34552
        'urllib3==1.26.4',          # CVE-2021-33503
        'Jinja2==3.0.1',            # CVE-2024-22195
        'lxml==4.6.3',              # CVE-2021-28957
        'celery==4.4.7',            # CVE-2021-23727
        'aiohttp==3.7.4',           # CVE-2021-21330
        'boto3==1.17.78',
        'redis==3.5.3',
        'psycopg2-binary==2.9.1',
        'gunicorn==20.1.0',
        'marshmallow==3.12.1',
        'pydantic==1.8.2',
        'click==7.1.2',
    ],

    # TC_40: extras_require — scanned for vulnerabilities
    extras_require={
        'dev': [
            'pytest==6.2.5',
            'black==21.7b0',
            'flake8==3.9.2',
            'mypy==0.910',
            'bandit==1.7.0',
        ],
        'docs': [
            'Sphinx==4.0.2',
            'sphinx-rtd-theme==0.5.2',
        ],
        'ml': [
            'numpy==1.19.4',        # CVE-2021-41495
            'pandas==1.2.4',
            'scikit-learn==0.24.2',
        ],
    },

    # TC_41: tests_require — scanned
    tests_require=[
        'nose==1.3.7',
        'coverage==5.5',
        'mock==4.0.3',
    ],

    entry_points={
        'console_scripts': [
            'vuln-app=api.app:main',
        ],
    },
)
