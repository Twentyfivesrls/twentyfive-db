package com.twentyfive.twentyfivedb.fidelity.wallet;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.spi.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Il registro dei plugin ImageIO è unico per tutta la JVM, quindi condiviso tra le
 * applicazioni dello stesso Tomcat. I plugin registrati da questa applicazione
 * (es. jai-imageio, incluso tramite zxing) restano registrati anche dopo un redeploy
 * e, puntando a un classloader fermato, fanno fallire ogni lettura di immagini
 * ("Illegal access: this web application instance has been stopped already").
 *
 * Allo spegnimento si rimuovono i plugin di questa applicazione; {@link #removeStaleProviders()}
 * rimuove quelli lasciati da applicazioni già fermate.
 */
@Component
@Slf4j
public class ImageIoProviderCleanup {

    private static final List<Class<?>> CATEGORIES = List.of(
            ImageReaderSpi.class, ImageWriterSpi.class, ImageTranscoderSpi.class,
            ImageInputStreamSpi.class, ImageOutputStreamSpi.class);

    @PreDestroy
    public void removeOwnProviders() {
        ClassLoader own = ImageIoProviderCleanup.class.getClassLoader();
        int removed = removeProviders(provider -> provider.getClass().getClassLoader() == own);
        if (removed > 0) {
            log.info("Rimossi {} plugin ImageIO di questa applicazione", removed);
        }
    }

    /** Rimuove i plugin appartenenti ad applicazioni Tomcat già fermate. */
    public static void removeStaleProviders() {
        int removed = removeProviders(provider -> isStoppedWebappLoader(provider.getClass().getClassLoader()));
        if (removed > 0) {
            log.warn("Rimossi {} plugin ImageIO di applicazioni già fermate", removed);
        }
    }

    private static int removeProviders(Predicate<Object> shouldRemove) {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        int removed = 0;
        for (Class<?> category : CATEGORIES) {
            removed += removeProviders(registry, category, shouldRemove);
        }
        return removed;
    }

    private static <T> int removeProviders(IIORegistry registry, Class<T> category, Predicate<Object> shouldRemove) {
        List<T> toRemove = new ArrayList<>();
        Iterator<T> providers = registry.getServiceProviders(category, false);
        while (providers.hasNext()) {
            T provider = providers.next();
            if (shouldRemove.test(provider)) {
                toRemove.add(provider);
            }
        }
        toRemove.forEach(provider -> registry.deregisterServiceProvider(provider, category));
        return toRemove.size();
    }

    /** Classloader di un'applicazione Tomcat non più avviata (letto via reflection per non dipendere da Tomcat). */
    private static boolean isStoppedWebappLoader(ClassLoader loader) {
        if (loader == null) {
            return false;
        }
        try {
            Method getStateName = loader.getClass().getMethod("getStateName");
            return !"STARTED".equals(getStateName.invoke(loader));
        } catch (ReflectiveOperationException e) {
            return false; // non è un classloader di Tomcat
        }
    }
}
