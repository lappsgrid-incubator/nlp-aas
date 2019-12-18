package org.lappsgrid.services.nlpaas.model

/**
 *
 */
class ServiceList implements Iterable<LappsgridService> {
    Map<String,LappsgridService> index = new HashMap<>()

    int size() { return index.size() }
    void add(LappsgridService service) {
        LappsgridService existing = index.get(service.serviceId)
        if (existing == null) {
            index.put(service.serviceId, service)
            return
        }
        if (service.version < existing.version) {
            return
        }
        index.remove(service.serviceId)
        index.put(service.serviceId, service)
    }

    Iterator<LappsgridService> iterator() {
        return new ServiceListIterator()
    }

    class ServiceListIterator implements Iterator<LappsgridService> {
        Iterator<Map.Entry<String,LappsgridService>> it = index.iterator()

        @Override
        boolean hasNext() {
            return it.hasNext()
        }

        @Override
        LappsgridService next() {
            Map.Entry<String,LappsgridService> entry = it.next()
            return entry.getValue()
        }
    }
}
