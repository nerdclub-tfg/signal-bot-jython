from de.nerdclubtfg.signalbot import SignalInterface as signal
from datetime import datetime
from signalbot import cron
import traceback

plugins = []

def init():
    cron.checkJobs()

def onMessage(sender, message, group):
    called = []
    for plugin in plugins:
        try:
            if plugin.enabled and plugin.accepts(sender, message, group):
                called.append(type(plugin).__name__)
                plugin.onMessage(sender, message, group)
        except Exception as e:
            signal.sendMessage(sender, group, 'Error: %s' % type(e).__name__)
            print('Unexpected error:\n')
            traceback.print_exc()
    
    if(len(called) > 0):
        print('[%s] %s%s: \"%s\" forwarded to %s' % (
                            timestampToString(message.getTimestamp()),
                            sender.getNumber(), 
                            (' in group %s' % group.getName()) if group != None else '', 
                            message.getBody().get() if message.getBody().isPresent() else 'no body', 
                            called))

def timestampToString(timestamp):
    return datetime.fromtimestamp(timestamp / 1000).strftime('%Y-%m-%d %H:%M:%S')

def getPlugin(name):
    return next(v for v in plugins if type(v).__name__.lower() == name)
