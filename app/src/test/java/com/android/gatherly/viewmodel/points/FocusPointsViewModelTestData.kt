package com.android.gatherly.viewmodel.points

import com.android.gatherly.model.points.Points
import com.android.gatherly.model.points.PointsSource
import com.google.firebase.Timestamp
import java.util.Calendar

object FocusPointsViewModelTestData {

  /** Calendar instance set to November 23, 2025 at 23:39:00 */
  val nov23 =
      Calendar.getInstance().apply {
        set(Calendar.YEAR, 2025)
        set(Calendar.MONTH, Calendar.NOVEMBER)
        set(Calendar.DAY_OF_MONTH, 23)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 39)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }

  /** Calendar instance set to November 21, 2025 at 23:39:00 */
  val nov21 =
      Calendar.getInstance().apply {
        set(Calendar.YEAR, 2025)
        set(Calendar.MONTH, Calendar.NOVEMBER)
        set(Calendar.DAY_OF_MONTH, 21)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 39)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }

  /** Sample points entry from timer source (22 minutes) obtained on November 21 */
  val points1 =
      Points(
          userId = "test-user",
          obtained = 23.9,
          reason = PointsSource.Timer(22),
          dateObtained = Timestamp(nov21.time))

  /** Sample points entry from badge source (bronze friends) obtained on November 23 */
  val points2 =
      Points(
          userId = "test-user",
          obtained = 30.0,
          reason = PointsSource.Badge("bronze friends"),
          dateObtained = Timestamp(nov23.time))
}
