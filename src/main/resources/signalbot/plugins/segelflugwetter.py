from de.nerdclubtfg.signalbot import SignalInterface as signal
from signalbot.patternplugin import PatternPlugin

from java.lang import String as jString

import urllib2

class Segelflugwetter(PatternPlugin):
    
    def __init__(self, enabled):
        PatternPlugin.__init__(self, enabled, '^!segelflugwetter .*')
    
    def onMessage(self, sender, message, group):
        body = message.getBody().get()[17:]
        req = urllib2.Request(
                'http://www.dwd.de/DE/fachnutzer/luftfahrt/teaser/luftsportberichte/fxdl40_%s_node.html' % body)
        
        html = None
        try:
            response = urllib2.urlopen(req)
            html = response.read()
            queryStart = '<article class=\"article-full\" role=\"article\"><div class=\"body-text\"><pre>'
            queryEnd = '</pre></div></article>'
            text = html[html.find(queryStart) + len(queryStart) : html.find(queryEnd)]
            signal.sendMessage(sender, jString(text, 'utf-8'))
        except urllib2.URLError as e:
            signal.sendMessage(sender, 'Error: %s' % e.reason)
        except urllib2.HTTPError as e:
            signal.sendMessage(sender, 'Error: %s' % e.code)
