# MetaTry - Development Documentation

This document tracks all feature implementations and changes for the MetaTry social media management application.

---

## Current Branches

- **Backend**: `main` (MetaTry repo)
- **Frontend**: `main` (MetaTryFront repo)

---

## Recent Changes Summary

### Session: Campaign Management - Add to Existing Campaign

**Date**: May 2026

**Goal**: Allow users to add new posts (AI-generated or manual) to existing campaigns instead of just creating new campaigns.

---

## Backend Changes (MetaTry)

### New Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/campaigns/recent?limit=N` | GET | Get last N recent campaigns (default 5) sorted by created date |
| `/campaigns/{campaignId}/generate` | POST | Generate AI posts for an existing campaign |

### Modified Files

#### 1. `CampaignController.java`
- Added `GET /campaigns/recent` endpoint
- Added `POST /campaigns/{campaignId}/generate` endpoint for generating posts on existing campaigns

```java
@GetMapping("/recent")
public List<CampaignDTO> getRecentCampaigns(@RequestParam(defaultValue = "5") int limit)

@PostMapping("/{campaignId}/generate")
public List<Post> generateForExistingCampaign(@PathVariable Long campaignId, @RequestBody Map<String, Object> body)
```

#### 2. `CampaignService.java`
- Added `getRecentCampaigns(int limit)` method
- Added `generatePostsForExistingCampaign(Long campaignId, int postNumber)` method

```java
public List<CampaignDTO> getRecentCampaigns(int limit)
public List<Post> generatePostsForExistingCampaign(Long campaignId, int postNumber)
```

#### 3. `CampaignDTO.java`
- Added `@Builder`, `@NoArgsConstructor` annotations
- Added `platform` and `status` fields

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignDTO {
    private Long id;
    private String name;
    private String topic;
    private String platform;
    private String status;
    private int postCount;
}
```

#### 4. `AiGeneratedContent.java`
- Added `@JsonIgnoreProperties(ignoreUnknown = true)` to handle unknown fields
- Added `twitterTitle`, `twitterPost`, `twitterHashtags` fields

---

## Frontend Changes (MetaTryFront)

### New Features

1. **Add to Existing Campaign Button**
   - Located in campaign page header
   - Opens the same form as "New Campaign" but in existing mode

2. **Campaign Selector Dropdown**
   - Uses `<datalist>` with last 5 recent campaigns
   - Type to search/filter campaigns
   - Shows topic when a campaign is selected

3. **Post Number Input**
   - Available for both new and existing campaign modes
   - Allows user to specify how many AI posts to generate

### Modified Files

#### 1. `campaign.service.ts`
- Added `getRecent(limit: number)` method
- Added `generateForExisting(campaignId: number, postNumber: number)` method

```typescript
getRecent(limit: number = 5): Observable<any[]>
generateForExisting(campaignId: number, postNumber: number): Observable<any[]>
```

#### 2. `campaigns.component.ts`
- Added `showExistingMode` boolean
- Added `recentCampaigns` array
- Added `selectedExistingCampaign` object
- Added lifecycle: `ngOnInit()` calls `loadRecentCampaigns()`
- Added methods:
  - `loadRecentCampaigns()` - fetches last 5 campaigns
  - `onCampaignSelect(event)` - handles selection, shows topic, loads posts
  - `enableExistingMode()` - switches to existing campaign mode
  - `enableNewMode()` - switches to new campaign mode
  - `generate()` - now handles both new and existing campaign generation

#### 3. `campaigns.component.html`
- Added header buttons: "Add to Existing" and "New Campaign"
- Added campaign selector with datalist
- Added topic display (read-only for existing campaigns)
- Added selected campaign info panel (name, platform, post count)
- Made post number input available for both modes

#### 4. `campaigns.component.css`
- Added styles for `.header-actions` (button container)
- Added styles for `.campaign-selector`
- Added styles for `.topic-display` (read-only input)
- Added styles for `.selected-info` (campaign details panel)

---

## Previous Features Implemented

### Dashboard Enhancements

1. **KPI Cards** - Total Posts, Published, Campaigns, Engagement
2. **Marketing Insights Panel** - Best posting times (FB/IG), weekly comparison, upcoming scheduled
3. **Admin Stats Panel** - Marketing members, active users, banned users
4. **Chart Section** - Area chart showing engagement over time (likes/comments)
5. **Campaign Progress** - Progress bars for active campaigns
6. **Recent Campaigns** - Grid of campaign cards with platform tags

### Content Calendar

- FullCalendar week view (7am-midnight scrollable)
- Marketing role only
- Dark mode support

### Backend API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/posts/calendar` | GET | Get posts for calendar view (ZonedDateTime) |
| `/posts/timing-analysis` | GET | Get best posting times by platform |
| `/posts/weekly-comparison` | GET | Compare this week vs last week posts |
| `/posts/upcoming-scheduled` | GET | Get upcoming scheduled posts |
| `/admin/stats` | GET | Get user statistics |
| `/admin/campaigns/progress` | GET | Get campaign progress |

---

## Tech Stack

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17
- **Database**: (Your current DB)
- **Port**: 8081

### Frontend
- **Framework**: Angular 19
- **Port**: 4200
- **Charts**: Chart.js
- **Icons**: Lucide Angular

---

## Running the Application

### Backend
```bash
cd metaTry
mvn spring-boot:run
```

### Frontend
```bash
cd metatry-front
npm start
```

---

## API Quick Reference

### Campaign Endpoints
```
GET    /campaigns              - Get all campaigns
GET    /campaigns/recent?limit=5 - Get recent campaigns
GET    /campaigns/{id}         - Get single campaign
POST   /campaigns              - Create new campaign
POST   /campaigns/generate    - Generate AI posts for new campaign
POST   /campaigns/{id}/generate - Generate AI posts for existing campaign
POST   /campaigns/{id}/posts/with-image - Create manual post
DELETE /campaigns/{id}         - Delete campaign
```

### Post Endpoints
```
GET    /posts/latestPublished?limit=15 - Get latest published posts
GET    /posts/top?limit=5       - Get top performing posts
GET    /posts/stats            - Get post statistics
GET    /posts/calendar?start=&end= - Get calendar events
GET    /posts/timing-analysis  - Get best posting times
GET    /posts/weekly-comparison - Get weekly comparison
GET    /posts/upcoming-scheduled?limit=3 - Get upcoming posts
POST   /publish/{postId}        - Publish a post
PUT    /posts/{id}              - Update post
DELETE /posts/{id}              - Delete post
POST   /posts/{id}/generate-image - Generate AI image
```

### Admin Endpoints
```
GET    /admin/stats            - Get user statistics
GET    /admin/campaigns/progress - Get campaign progress
```

---

## Notes

- Role-based access: ADMIN and MARKETING roles
- Dark mode support via `body.dark-mode` CSS class
- ZonedDateTime used for calendar endpoints (ISO format with timezone)
- Campaign selector shows last 5 campaigns by default