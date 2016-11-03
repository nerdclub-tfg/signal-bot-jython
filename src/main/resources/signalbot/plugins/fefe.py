# -*- coding: UTF-8 -*-

from de.nerdclubtfg.signalbot import SignalInterface as signal
from signalbot.patternplugin import PatternPlugin

from java.lang import String as jString

import urllib2
import re

class Fefe(PatternPlugin):
    
    def __init__(self, enabled):
        PatternPlugin.__init__(self, enabled, '^!fefe .*')
        self.paramPattern = re.compile('[a-z0-9]{8}')
        self.queryStart = re.compile('<li><a href=\"\\?ts=[a-z0-9]{8}\">\\[l\\]</a>')
    
    def onMessage(self, sender, message, group):
        param = message.getBody().get()[6:]
        if not self.paramPattern.match(param):
            signal.sendMessage(sender, group, jString('Fehler: Keine gültige Fefe-ID!'))
            return
        try:
            # download html
            req = urllib2.Request('http://blog.fefe.de/?ts=%s' % param)
            response = urllib2.urlopen(req)
            html = response.read()
            
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
            signal.sendMessage(sender, group, jString(text, 'utf-8'))
            # error handling
        except urllib2.URLError as e:
            signal.sendMessage(sender, group, 'Fehler: %s' % e.reason)
        except urllib2.HTTPError as e:
            signal.sendMessage(sender, group, 'Fehler: %s' % e.code)
