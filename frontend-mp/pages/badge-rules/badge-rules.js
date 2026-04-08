const badgeApi = require('../../api/badge');
const flowerApi = require('../../api/flower');

Page({
  data: {
    badges: [],
    myBadges: [],
    currentLevel: 0,
    currentFlowers: 0,
    loading: true
  },

  onLoad() {
    Promise.all([
      badgeApi.my().then(r => r.data),
      flowerApi.summary().then(r => r.data)
    ]).then(([badgeData, summary]) => {
      const badges = (badgeData && badgeData.badges) || [];
      const myBadges = (badgeData && badgeData.myBadges) || [];
      const myLevels = myBadges.map(b => b.badgeLevel);
      badges.forEach(b => { b.owned = myLevels.includes(b.level); });
      this.setData({
        badges,
        myBadges,
        currentLevel: (summary && summary.badgeLevel) || 0,
        currentFlowers: (summary && summary.redFlowerTotal) || 0,
        loading: false
      });
    }).catch(() => this.setData({ loading: false }));
  }
});
