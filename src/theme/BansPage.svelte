<div class="container mt-4">
  <h2 class="mb-4">
    {$_('bans.title', { default: 'Bans' })}
    {#if config.showTotalBans && pagination.total > 0}
      <small class="text-muted ms-2">({pagination.total})</small>
    {/if}
  </h2>

  {#if bans.length === 0}
    <div class="alert alert-info">
      {$_('bans.empty', { default: 'No active bans found.' })}
    </div>
  {:else}
    <div class:row={config.viewLayout === 'GRID'} class:list-group={config.viewLayout === 'LIST'}>
      {#each bans as ban}
        {#if config.viewLayout === 'GRID'}
          <div class="col-md-6 col-lg-4 mb-3">
            <a
              href={`/player/${ban.username}`}
              class="card h-100 text-decoration-none text-body transition-transform">
              <div class="card-body text-center">
                {#if config.showAvatars}
                  <div class="mb-3">
                    <PlayerHead
                      username={ban.username}
                      width={config.avatarSize === 'PX_16'
                        ? 16
                        : config.avatarSize === 'PX_32'
                          ? 32
                          : 64}
                      height={config.avatarSize === 'PX_16'
                        ? 16
                        : config.avatarSize === 'PX_32'
                          ? 32
                          : 64}
                      banned={isActiveBan(ban)}
                      {checkTime} />
                  </div>
                {/if}
                <h5 class="card-title">
                  {ban.username}
                </h5>

                {#if config.showReason}
                  <p class="card-text">
                    <span class="badge bg-danger"
                      >{$_('bans.reason', { default: 'Reason' })}: {ban.banMessage || 'N/A'}</span>
                  </p>
                {/if}

                <p class="mb-0">
                  {#if config.showDuration && ban.banDate}
                    <i class="bi bi-clock"></i>
                    {$_('bans.banned_on', { default: 'Banned on' })}: <PanoDate
                      time={ban.banDate} /><br />
                  {/if}
                  {#if config.showExpiry}
                    <i class="bi bi-calendar-x"></i>
                    {$_('bans.expires', { default: 'Expires' })}:
                    {#if ban.bannedUntil}
                      <PanoDate time={ban.bannedUntil} />
                    {:else}
                      {$_('bans.permanent', { default: 'Permanent' })}
                    {/if}
                  {/if}
                </p>
              </div>
            </a>
          </div>
        {:else}
          <a
            href={`/player/${ban.username}`}
            class="list-group-item list-group-item-action d-flex align-items-center text-decoration-none text-body">
            {#if config.showAvatars}
              <div class="me-3">
                <PlayerHead
                  username={ban.username}
                  width={config.avatarSize === 'PX_16'
                    ? 16
                    : config.avatarSize === 'PX_32'
                      ? 32
                      : 64}
                  height={config.avatarSize === 'PX_16'
                    ? 16
                    : config.avatarSize === 'PX_32'
                      ? 32
                      : 64}
                  banned={isActiveBan(ban)}
                  lastActivityTime={ban.lastActivityTime}
                  {checkTime} />
              </div>
            {/if}

            <div class="flex-grow-1">
              <div class="d-flex justify-content-between align-items-center">
                <h5>{ban.username}</h5>
              </div>

              <div class="mb-2">
                {#if config.showReason}
                  <span class="badge text-bg-danger focus-ring me-2"
                    >{$_('bans.reason', { default: 'Reason' })}: {ban.banMessage || 'N/A'}</span>
                {/if}
              </div>

              <small class="text-gray">
                {#if config.showDuration && ban.banDate}
                  <span class="me-3">
                    <i class="bi bi-clock"></i>
                    {$_('bans.banned_on', { default: 'Banned on' })}: <PanoDate
                      time={ban.banDate} />
                  </span>
                {/if}
                {#if config.showExpiry}
                  <span>
                    <i class="bi bi-calendar-x"></i>
                    {$_('bans.expires', { default: 'Expires' })}:
                    {#if ban.bannedUntil}
                      <PanoDate time={ban.bannedUntil} />
                    {:else}
                      {$_('bans.permanent', { default: 'Permanent' })}
                    {/if}
                  </span>
                {/if}
              </small>
            </div>
          </a>
        {/if}
      {/each}
    </div>

    <!-- Pagination -->
    {#if pagination.total > 0}
      <div class="mt-4">
        <Pagination
          page={pagination.current}
          total={pagination.last}
          on:change={(e) => loadBans(e.detail)} />
      </div>
    {/if}
  {/if}
</div>

<script context="module">
  import ApiUtil, { buildQueryParams } from '@panomc/sdk/utils/api';

  export async function load(event) {
    const {
      url: { searchParams },
    } = event;
    const page = parseInt(searchParams.get('page') || '1');

    try {
      const queryParams = buildQueryParams({ page });
      const res = await ApiUtil.get({
        path: `/api/bans${queryParams}`,
        request: event,
      });

      console.log(res);

      return {
        data: {
          bans: res.bans,
          config: res.config,
          pagination: res.pagination,
        },
      };
    } catch (e) {
      console.error(e);
      return {
        data: {
          bans: [],
          config: {},
          pagination: { current: 1, last: 1, total: 0, perPage: 20 },
        },
      };
    }
  }
</script>

<script>
  import { onMount } from 'svelte';
  import { goto, page } from '@panomc/sdk/svelte';
  import { _ } from '../main';
  import { Pagination, Date as PanoDate, PlayerHead } from '@panomc/sdk/components/theme';

  export let data;
  const { bans, config, pagination } = data;

  async function loadBans(pageNum) {
    const url = new URL($page.url);
    url.searchParams.set('page', pageNum.toString());
    await goto(url.toString());
  }

  function isActiveBan(ban) {
    if (!ban.banned) return false;
    if (ban.bannedUntil && ban.bannedUntil < Date.now()) return false;
    return true;
  }

  let checkTime = 0;
  let interval;

  onMount(() => {
    interval = setInterval(() => {
      checkTime += 1;
    }, 1000);

    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  });
</script>
