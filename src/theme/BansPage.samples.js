// Sample data of BansPage for the view catalogue (doc 02 section 7). Pure data: import only view helpers and
// relative .js fixtures.
/** @type {string[]} */
export const notApplicable = ['error', 'loading'];

const config = {
  showSearch: true,
  showAvatars: true,
  avatarSize: 'PX_32',
  showReason: true,
  showDuration: true,
  showExpiry: true,
  viewLayout: 'LIST',
};

/** @type {import('@panomc/plugin-kit').Samples} */
export default {
  filled: {
    props: {
      data: {
        bans: [
          { username: 'Steve', banMessage: 'Griefing', banDate: 1760000000000, bannedUntil: null },
          { username: 'Alex', banMessage: 'Spam in chat', banDate: 1760500000000, bannedUntil: 1770000000000 },
        ],
        config,
        pagination: { current: 1, last: 1, total: 2, perPage: 20 },
        search: '',
      },
    },
  },
  empty: {
    props: {
      data: {
        bans: [],
        config,
        pagination: { current: 1, last: 1, total: 0, perPage: 20 },
        search: '',
      },
    },
  },
};
