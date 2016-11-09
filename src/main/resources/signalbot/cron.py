from de.nerdclubtfg.signalbot import Storage
import bot

from java.lang import System

from threading import Timer

storageName = 'cron'
currentTimer = None
currentNext = None

def _getStore():
    store = Storage.load(storageName)
    if store == None:
        store = {'jobs': []}
    return store

def after(plugin, millis, event):
    at(plugin, System.currentTimeMillis() + millis, event)

def at(plugin, epoch, event):
    store = _getStore()
    store['jobs'].append({
        'plugin': type(plugin).__name__.lower(),
        'at': epoch, 
        'event': event})
    Storage.save(storageName, store)
    checkJobs()
    
def checkJobs():
    global currentTimer
    global currentNext
    store = _getStore()
    nextJob = None
    time = System.currentTimeMillis()
    executed = []
    for job in store['jobs']:
        if job['at'] <= time:
            plugin = bot.getPlugin(job['plugin'])
            if plugin != None:
                print('Executing cron job for %s' % job['plugin'])
                plugin.callback(job['event'])
            else:
                print('Warning: Plugin %s not found for job! This can happen after removing plugins.' % job['plugin'])
            executed.append(job)
            if currentNext == job:
                currentNext = None
        elif nextJob == None or nextJob['at'] > job['at']:
            nextJob = job
    store['jobs'] = [job for job in store['jobs'] if not job in executed]
    Storage.save(storageName, store)
    
    if nextJob != None:
        if currentNext == None or currentNext['at'] > nextJob['at']:
            wait = max((nextJob['at'] - System.currentTimeMillis()) / 1000.0, 0)
            if currentTimer != None:
                currentTimer.cancel()
            currentNext = nextJob
            currentTimer = Timer(wait, checkJobs)
            currentTimer.start()
