# -*- coding: UTF-8 -*-

from de.nerdclubtfg.signalbot import SignalInterface as signal
from signalbot.patternplugin import PatternPlugin

from java.lang import String as jString
from java.lang import ClassLoader
from java.net import URL
from java.security import KeyStore
from java.util import Scanner
from javax.net.ssl import TrustManagerFactory, SSLContext

import re


class Fefe(PatternPlugin):
    
    def __init__(self, enabled):
        PatternPlugin.__init__(self, enabled, '^!fefe .*')
        self.paramPattern = re.compile('[a-z0-9]{8}')
        self.queryStart = re.compile('<li><a href=\"\\?ts=[a-z0-9]{8}\">\\[l\\]</a>')
        self.initHttps()
    
    def onMessage(self, sender, message, group):
        param = message.getBody().get()[6:]
        if not self.paramPattern.match(param):
            signal.sendMessage(sender, group, jString('Fehler: Keine gültige Fefe-ID!'))
            return
        try:
            # download html
            html = self.readHttps('https://blog.fefe.de/?ts=%s' % param)
            
            # find article
            queryStartIndex = self.queryStart.search(html).start()
            queryEnd = '</ul>'
            queryEndIndex = html.find(queryEnd, queryStartIndex)
            text = html[queryStartIndex + 35 : queryEndIndex] # 32 = len(queryStart)
            
            # replace <p> with newline and <i> and <b> with * (Markdown-like)
            text = text.replace('<p>', '\n\n').replace('<p u>', '\n\n')
            text = text.replace('<b>', '*').replace('</b>', '*')
            text = text.replace('<i>', '*').replace('</i>', '*')
            # format links
            text = text.replace('<a href=\"', '(').replace('\">', ')[').replace('</a>', ']')
            # format quotes
            text = text.replace('<blockquote>', '\n\n> ').replace('</blockquote>', '\n\n')
            
            # send response
            signal.sendMessage(sender, group, text)
            # error handling
        except Exception as e:
            signal.sendMessage(sender, group, 'Fehler: %s' % e)

    def initHttps(self):
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        trustStoreIn = ClassLoader.getSystemClassLoader().getResourceAsStream('isrgrootx1.keystore')
        keyStore.load(trustStoreIn, jString('letsencrypt').toCharArray())
        trustStoreIn.close()
        
        trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        context = SSLContext.getInstance('TLS')
        context.init(None, trustManagerFactory.getTrustManagers(), None)
        
        self.socketFactory = context.getSocketFactory()

    def readHttps(self, url):
        connection = URL(url).openConnection()
        connection.setSSLSocketFactory(self.socketFactory)
        connectionIn = connection.getInputStream()
        
        scan = Scanner(connectionIn, 'utf-8').useDelimiter('\\A')
        html = scan.next() # reads whole stream
        scan.close()
        return html
