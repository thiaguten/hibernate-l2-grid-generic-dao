package br.com.thiaguten.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;

/**
 * Cache PersistenceProviderResolver per classloader and use the current
 * classloader as a key. Use CachingPersistenceProviderResolver for each
 * PersistenceProviderResolver instance.
 *
 * @author Emmanuel Bernard
 * @author Thiago Gutenberg
 */
public class PersistenceProviderResolverImpl implements PersistenceProviderResolver {

    private PersistenceProviderResolverImpl() {
        // singleton
    }

    /**
     * Initialization-on-demand holder idiom implementation.
     *
     * @author Thiago Gutenberg Carvalho da Costa
     */
    private static class LazySingletonHolder {
        private static final PersistenceProviderResolverImpl INSTANCE = new PersistenceProviderResolverImpl();
    }

    /**
     * Get persistence provider resolver instance.
     *
     * @return persistence provider resolver singleton instance
     */
    public static PersistenceProviderResolverImpl getInstance() {
        return LazySingletonHolder.INSTANCE;
    }

    // FIXME use a ConcurrentHashMap with weak entry
    private final WeakHashMap<ClassLoader, PersistenceProviderResolver> resolvers = new WeakHashMap<ClassLoader, PersistenceProviderResolver>();
    private volatile short barrier = 1;

    /**
     * {@inheritDoc}
     */
    public List<PersistenceProvider> getPersistenceProviders() {
        ClassLoader cl = getContextualClassLoader();
        if (barrier == 1) {
        } // read barrier syncs state with other threads
        PersistenceProviderResolver currentResolver = resolvers.get(cl);
        if (currentResolver == null) {
            currentResolver = new CachingPersistenceProviderResolver(cl);
            resolvers.put(cl, currentResolver);
            barrier = 1;
        }
        return currentResolver.getPersistenceProviders();
    }

    /**
     * {@inheritDoc}
     */
    public void clearCachedProviders() {
        // todo : should we clear all providers from all resolvers here?
        ClassLoader cl = getContextualClassLoader();
        if (barrier == 1) {
        } // read barrier syncs state with other threads
        PersistenceProviderResolver currentResolver = resolvers.get(cl);
        if (currentResolver != null) {
            currentResolver.clearCachedProviders();
        }
    }

    private static ClassLoader getContextualClassLoader() {
        /*
         * ClassLoader cl = Thread.currentThread().getContextClassLoader(); if ( cl ==
         * null ) { cl = ApplicationPersistenceProviderResolver.class.getClassLoader();
         * } return cl;
         */

        return PersistenceProviderResolverImpl.class.getClassLoader();
    }

    /**
     * Resolve the list of Persistence providers for a given classloader and cache
     * the results.
     * <p>
     * Avoids to keep any reference from this class to the classloader being passed
     * to the constructor.
     *
     * @author Emmanuel Bernard
     */
    private static class CachingPersistenceProviderResolver implements PersistenceProviderResolver {
        // this assumes that the class loader keeps the list of classes loaded
        private final List<WeakReference<Class<? extends PersistenceProvider>>> resolverClasses = new ArrayList<WeakReference<Class<? extends PersistenceProvider>>>();

        public CachingPersistenceProviderResolver(ClassLoader cl) {
            loadResolverClasses(cl);
        }

        private void loadResolverClasses(ClassLoader cl) {
            synchronized (resolverClasses) {
                try {
                    Enumeration<URL> resources = cl
                            .getResources("META-INF/services/" + PersistenceProvider.class.getName());
                    Set<String> names = new HashSet<String>();
                    while (resources.hasMoreElements()) {
                        URL url = resources.nextElement();
                        InputStream is = url.openStream();
                        try {
                            names.addAll(providerNamesFromReader(new BufferedReader(new InputStreamReader(is))));
                        } finally {
                            is.close();
                        }
                    }
                    for (String s : names) {
                        @SuppressWarnings("unchecked")
                        Class<? extends PersistenceProvider> providerClass = (Class<? extends PersistenceProvider>) cl
                                .loadClass(s);
                        WeakReference<Class<? extends PersistenceProvider>> reference = new WeakReference<Class<? extends PersistenceProvider>>(
                                providerClass);
                        // keep Hibernate atop
                        if (s.endsWith("HibernatePersistence") && resolverClasses.size() > 0) {
                            WeakReference<Class<? extends PersistenceProvider>> movedReference = resolverClasses.get(0);
                            resolverClasses.add(0, reference);
                            resolverClasses.add(movedReference);
                        } else {
                            resolverClasses.add(reference);
                        }
                    }
                } catch (IOException e) {
                    throw new PersistenceException(e);
                } catch (ClassNotFoundException e) {
                    throw new PersistenceException(e);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<PersistenceProvider> getPersistenceProviders() {
            // TODO find a way to cache property instances
            // problem #1: avoid hard ref with classloader (List<WR<PP>>?
            // problem #2: avoid half GC lists
            // todo (steve) : why arent we just caching the PersistenceProvider *instances*
            // as the CachingPersistenceProviderResolver state???
            synchronized (resolverClasses) {
                List<PersistenceProvider> providers = new ArrayList<PersistenceProvider>(resolverClasses.size());
                try {
                    for (WeakReference<Class<? extends PersistenceProvider>> providerClass : resolverClasses) {
                        providers.add(providerClass.get().newInstance());
                    }
                } catch (InstantiationException e) {
                    throw new PersistenceException(e);
                } catch (IllegalAccessException e) {
                    throw new PersistenceException(e);
                }
                return providers;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized void clearCachedProviders() {
            synchronized (resolverClasses) {
                resolverClasses.clear();
                loadResolverClasses(PersistenceProviderResolverImpl.getContextualClassLoader());
            }
        }

        private static final Pattern nonCommentPattern = Pattern.compile("^([^#]+)");

        private static Set<String> providerNamesFromReader(BufferedReader reader) throws IOException {
            Set<String> names = new HashSet<String>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                Matcher m = nonCommentPattern.matcher(line);
                if (m.find()) {
                    names.add(m.group().trim());
                }
            }
            return names;
        }

    }
}
