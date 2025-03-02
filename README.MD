# MetaDeck API Proxy

hosts an IGDB proxy for use by MetaDeck

## Installation

Make sure you have a jvm installed

Grab the latest release and unpack it

[https://github.com/EmuDeck/metadeck-igdb-proxy/releases/latest]()

```shell
curl -s https://api.github.com/repos/EmuDeck/metadeck-igdb-proxy/releases/latest \
    | grep "browser_download_url.*tar" \
    | cut -d : -f 2,3 \
    | tr -d \" \
    | wget -i - && \
    tar -xf *.tar && \
    cd metadeck-igdb-proxy-*
```
(Don't panic, you don't need to use bash magic if that's not your style, just download the zip release and unzip like normal)

create a config file with the required twitch developer client id and secret, which you can obtain by following their
docs here [https://api-docs.igdb.com/#getting-started]()


`settings.json`
```json
{
    "clientId": "...",
    "clientSecret": "..."
}
```

And run it!

```shell
./bin/metadeck-igdb-proxy
```

On a local Windows machine you can also just put the config file in the bin folder and double-click the bat file, the config just needs to be in the current working directory

This starts an http server on port 8083, this is what MetaDeck will connect to, 
though if you are exposing it to the internet you should use a reverse proxy like Nginx so that you can have https and use port 80