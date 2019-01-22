/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.permissions;

import com.google.common.collect.Maps;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * An object to pass around that contains a {@link Subject} and caches all permission checks within it.
 *
 * @param <S> The type of {@link Subject}
 */
@NonnullByDefault
public class SubjectPermissionCache<S extends Subject> implements Subject {

    private final S subject;
    private final Map<String, Boolean> permissionCache = Maps.newHashMap();
    private final Map<String, Optional<String>> optionCache = Maps.newHashMap();

    public SubjectPermissionCache(S subject) {
        this.subject = subject;
    }

    public SubjectPermissionCache(S subject, SubjectPermissionCache<? extends Subject> subjectCache) {
        this(subject);
        permissionsFrom(subjectCache);
    }

    public SubjectPermissionCache<S> permissionsFrom(SubjectPermissionCache<? extends Subject> s) {
        this.permissionCache.putAll(s.permissionCache);
        this.optionCache.putAll(s.optionCache);
        return this;
    }

    public S getSubject() {
        return subject;
    }

    public void setPermissionOverride(String permission, boolean override) {
        permissionCache.put(permission, override);
    }

    @Override public Optional<CommandSource> getCommandSource() {
        return this.subject.getCommandSource();
    }

    @Override public SubjectCollection getContainingCollection() {
        return this.subject.getContainingCollection();
    }

    @Override public SubjectData getSubjectData() {
        return this.subject.getSubjectData();
    }

    @Override public SubjectData getTransientSubjectData() {
        return this.subject.getTransientSubjectData();
    }

    // Overriden for performance reasons.
    @Override public boolean hasPermission(Set<Context> contexts, String permission) {
        return permissionCache.computeIfAbsent(permission.toLowerCase(), k -> this.subject.hasPermission(contexts, k));
    }

    @Override public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        return permissionCache.computeIfAbsent(permission.toLowerCase(), k -> this.subject.hasPermission(contexts, k))
                ? Tristate.TRUE : Tristate.FALSE;
    }

    @Override public boolean isChildOf(Set<Context> contexts, Subject parent) {
        return subject.isChildOf(contexts, parent);
    }

    @Override public List<Subject> getParents(Set<Context> contexts) {
        return subject.getParents(contexts);
    }

    @Override public Optional<String> getOption(Set<Context> contexts, String key) {
        return optionCache.computeIfAbsent(key.toLowerCase(), subject::getOption);
    }

    @Override public String getIdentifier() {
        return subject.getIdentifier();
    }

    @Override public Set<Context> getActiveContexts() {
        return this.subject.getActiveContexts();
    }
}
